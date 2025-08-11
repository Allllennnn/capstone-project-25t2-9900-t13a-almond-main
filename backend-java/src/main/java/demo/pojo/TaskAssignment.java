package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task assignment POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignment {
    private Long id;                // Primary key
    private Long taskId;            // Task ID (foreign key)
    private Long userId;            // User ID (foreign key)
    private String role;            // Role (leader, developer, tester, etc.)
    private String description;     // Task description
    private Long assignedBy;        // Assigned by user ID
    private LocalDateTime assignedAt; // Assignment timestamp
    private LocalDateTime updatedAt;  // Last updated timestamp
    private String status;          // Status: DRAFT, SUBMITTED, FINALIZED
}
