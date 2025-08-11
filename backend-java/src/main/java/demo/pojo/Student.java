package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Student POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    private Long userId;      // User ID (foreign key)
    private String studentNo; // Student number
}
