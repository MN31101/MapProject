package languages.map.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;


import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class WSController {
    private static final Logger logger = LoggerFactory.getLogger(WSController.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public WSController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/public")
    public WebSocketMessage sendMessage(@Payload WebSocketMessage message,
                                        SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.debug("Received message: {}", message);
            // Add username in web socket session
            headerAccessor.getSessionAttributes().put("username", message.getSender());
            return message;
        } catch (Exception e) {
            logger.error("Error processing message", e);
            throw e;
        }
    }

    @MessageMapping("/sendPrivateMessage")
    public void sendPrivateMessage(@Payload WebSocketMessage message) {
        try {
            logger.debug("Sending private message to: {}", message.getRecipient());
            messagingTemplate.convertAndSendToUser(
                    message.getRecipient(),
                    "/queue/private",
                    message
            );
        } catch (Exception e) {
            logger.error("Error sending private message", e);
            throw e;
        }
    }

    @SubscribeMapping("/topic/status")
    public String sendStatus() {
        return "WebSocket connection established";
    }

    @MessageMapping("/register")
    public void register(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        if (sessionId != null) {
            logger.info("User registered: {} with session: {}", principal.getName(), sessionId);
            sessions.put(sessionId, new WebSocketSession(principal.getName(), sessionId));
        }
    }

    @Scheduled(fixedRate = 60000) // Every minute
    public void sendHeartbeat() {
        try {
            messagingTemplate.convertAndSend("/topic/heartbeat", "ping");
            logger.debug("Heartbeat sent");
        } catch (Exception e) {
            logger.error("Error sending heartbeat", e);
        }
    }

    // Helper class for session management
    private static class WebSocketSession {
        private final String username;
        private final String sessionId;

        public WebSocketSession(String username, String sessionId) {
            this.username = username;
            this.sessionId = sessionId;
        }
    }
}