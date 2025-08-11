package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * Teacher POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
    private Long userId;             // User ID (foreign key)
}