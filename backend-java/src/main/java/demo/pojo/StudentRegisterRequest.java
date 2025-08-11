package demo.pojo;

import lombok.Data;

/**
 * Student registration request parameters
 */
@Data
public class StudentRegisterRequest {
    private String username;   // Login username
    private String password;   // Plain-text password
    private String name;       // Full name
    private String email;      // Email address
    private String phone;      // Phone number
    private String studentNo;  // Student number
}
