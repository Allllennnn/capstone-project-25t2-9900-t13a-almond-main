package demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    private Long id;
    private Long taskId;
    private Long groupId;
    private Long senderId;
    private String senderType; // USER or AGENT
    private String content;
    private LocalDateTime createdAt;
} 