package demo.pojo;

import lombok.Data;

/**
 * Teacher registration request parameters
 */
@Data
public class TeacherRegisterRequest {
    private String username;   // Login username
    private String password;   // Plain-text password
    private String name;       // Full name
    private String email;      // Email address
    private String phone;      // Phone number
}
