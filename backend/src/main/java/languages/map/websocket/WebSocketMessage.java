package languages.map.websocket;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketMessage {
    private String content;
    private String sender;
    private String recipient;
    private MessageType type;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}