package demo.pojo;

import lombok.Data;

/**
 * Login request parameters
 */
@Data
public class LoginRequest {
    private String username;  // Login username
    private String password;  // Plain-text password
    private String role;      // Role: ADMIN / TEACHER / STUDENT
}
