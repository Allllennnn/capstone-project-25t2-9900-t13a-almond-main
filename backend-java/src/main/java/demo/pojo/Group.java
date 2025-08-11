package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Group POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    private Long id;                 // Group ID
    private String name;             // Group name
    private String description;      // Group description
    private Long teacherId;          // Teacher ID (foreign key)
    private LocalDateTime createdAt; // Creation timestamp
    private LocalDateTime updatedAt; // Last update timestamp

    // List of user IDs for group members, used only when creating a group
    private List<Long> members;
}
