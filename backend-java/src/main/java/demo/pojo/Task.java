package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Long id;                 // Task ID
    private String title;            // Task title
    private String description;      // Task description
    private Long groupId;            // Group ID (foreign key)
    private String status;           // Task status (INITIALIZING, IN_PROGRESS, COMPLETED)
    private LocalDate dueDate;       // Due date
    private Integer cycle;           // Task cycle (in weeks)
    private LocalDateTime createdAt; // Creation timestamp
    private LocalDateTime updatedAt; // Last update timestamp
    private String fileUrl;          // Attachment URL
}
