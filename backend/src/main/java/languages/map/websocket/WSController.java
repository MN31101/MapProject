package languages.map.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class WSController {
    private final SimpMessagingTemplate messagingTemplate;

    public WSController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sendTestMessage")
    public void sendUpdateNotificationToAllUsers(@Payload String message){
        messagingTemplate.convertAndSendToUser("test", "/message", message);
    }
    @Scheduled(cron = "0 */1 * * * *")
    public void trigger(){
        sendUpdateNotificationToAllUsers("hi from server lol");
    }

}
