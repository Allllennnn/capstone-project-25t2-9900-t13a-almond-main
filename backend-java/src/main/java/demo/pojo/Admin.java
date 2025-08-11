package demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Admin POJO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    private Long userId;             // 用户ID (外键)
}