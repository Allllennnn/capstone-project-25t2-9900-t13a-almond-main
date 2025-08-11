package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * General User POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;                 // User ID
    private String name;             // User full name
    private String username;         // Login username
    private String email;            // Email address
    private String phone;            // Phone number
    private String password;         // Password (encrypted)
    private String role;             // Role (ADMIN / TEACHER / STUDENT)
    private String status;           // Approval status (PENDING / ACTIVE)
    private LocalDateTime createdAt; // Creation timestamp
    private LocalDateTime updatedAt; // Last update timestamp
    private String studentNo;        // Student number (applicable for students only)
}
