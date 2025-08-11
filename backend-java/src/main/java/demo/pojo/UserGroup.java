package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * User–Group association POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGroup {
    private Long userId;      // User ID (foreign key)
    private Long groupId;     // Group ID (foreign key)
    private LocalDateTime assignedAt; // Assignment timestamp
}
