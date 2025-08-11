package demo.mapper;

import demo.pojo.User;
import demo.pojo.Group;
import demo.pojo.Task;
import demo.pojo.TaskAssignment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * General User Mapper
 */
@Mapper
public interface UserMapper {

    /**
     * Find users by role
     */
    @Select("SELECT * FROM users WHERE role = #{role}")
    List<User> findByRole(@Param("role") String role);

    /**
     * Find users by role and status
     */
    @Select("SELECT * FROM users WHERE role = #{role} AND status = #{status}")
    List<User> findByRoleAndStatus(@Param("role") String role,
                                   @Param("status") String status);

    /**
     * Update user status
     */
    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id,
                      @Param("status") String status);

    /**
     * Delete user by ID
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    /**
     * Insert a new user
     */
    @Insert("INSERT INTO users (username, password, name, email, phone, role, status, created_at, updated_at) " +
            "VALUES (#{username}, #{password}, #{name}, #{email}, #{phone}, #{role}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    /**
     * Update existing user
     */
    @Update("UPDATE users SET username = #{username}, name = #{name}, email = #{email}, " +
            "phone = #{phone}, status = #{status}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    void update(User user);

    /**
     * Find users by role with additional filters
     */
    List<User> findByRoleAndFilter(@Param("role") String role,
                                   @Param("name") String name,
                                   @Param("id")   Long id);

    /**
     * Conditional query for students
     */
    List<User> findStudentsByCondition(@Param("name") String name,
                                       @Param("id") Long id);

    /**
     * Conditional query for teachers
     */
    List<User> findTeachersByCondition(@Param("name") String name,
                                       @Param("id") Long id);

    /**
     * Count user registrations by day
     */
    List<Map<String, Object>> countUserRegistrationByDay();

    /**
     * Retrieve recent activities
     */
    List<Map<String, Object>> getRecentActivities();

    /**
     * Count users by role
     */
    @Select("SELECT COUNT(*) FROM users WHERE role = #{role}")
    Long countByRole(@Param("role") String role);

    /**
     * Find user by ID
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(@Param("id") Long id);

    /**
     * Find user by username
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * Insert student information into students table
     */
    @Insert("INSERT INTO students (user_id, student_no) VALUES (#{userId}, #{studentNo})")
    void insertStudentInfo(@Param("userId") Long userId, @Param("studentNo") String studentNo);

    /**
     * Find user by student number
     */
    @Select("SELECT u.* FROM users u JOIN students s ON u.id = s.user_id WHERE s.student_no = #{studentNo}")
    User findByStudentNo(@Param("studentNo") String studentNo);

    /**
     * Retrieve student number by user ID
     */
    @Select("SELECT student_no FROM students WHERE user_id = #{userId}")
    String getStudentNoByUserId(@Param("userId") Long userId);

    /**
     * Update student information
     */
    @Update("UPDATE students SET student_no = #{studentNo} WHERE user_id = #{userId}")
    void updateStudentInfo(@Param("userId") Long userId, @Param("studentNo") String studentNo);
}
