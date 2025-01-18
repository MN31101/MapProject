package languages.map.websocket;

import lombok.Data;

@Data
public class Message {
    private MessageType type;
    private String content;
    private String sender;
}

enum MessageType{
    CHAT,
    JOIN,
    LEAVE
}
