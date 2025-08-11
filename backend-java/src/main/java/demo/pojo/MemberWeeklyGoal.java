package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberWeeklyGoal {
    private Long id;
    private Long taskId;
    private Long studentId;
    private Integer weekNo;
    private String goal;
    private String status; // NOTUPLOADED, PROCESSING, FINISHED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 