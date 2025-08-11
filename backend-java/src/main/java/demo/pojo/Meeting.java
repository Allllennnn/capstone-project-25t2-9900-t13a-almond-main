package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Meeting POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    private Long id;                    // Primary key
    private Long groupId;               // Group ID
    private Long taskId;                // Task ID
    private Integer meetingNo;          // Meeting number
    private LocalDate meetingDate;      // Date of the meeting
    private String status;              // Status: UNFINISHED, COMPLETED
    private String documentUrl;         // URL of the meeting document
    private LocalDateTime createdAt;    // Creation timestamp
    private LocalDateTime updatedAt;    // Last update timestamp
}
