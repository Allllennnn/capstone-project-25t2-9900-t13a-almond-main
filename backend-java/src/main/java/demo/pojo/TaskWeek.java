package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Task weekly goal POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskWeek {
    private Long id;                 // Primary key
    private Long taskId;             // Task ID (foreign key)
    private Integer weekNo;          // Week number
    private String goal;             // Description of the weekly goal
    private LocalDateTime createdAt; // Creation timestamp
    private LocalDateTime updatedAt; // Last update timestamp
}
