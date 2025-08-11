package demo.service;

import demo.pojo.PageBean;
import demo.pojo.User;
import demo.pojo.Group;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Administrator service interface: CRUD and approval for teachers, students, and groups
 */
public interface AdminService {
    // --- Student-related operations ---
    List<User> getAllStudents();
    PageBean<User> pageStudents(Integer page, Integer pageSize, String name, Long id);
    void createStudent(User student);
    void updateStudent(User student);
    void deleteStudent(Long studentId);

    // --- Teacher-related operations ---
    List<User> getAllTeachers();
    PageBean<User> pageTeachers(Integer page, Integer pageSize, String name, Long id);
    void createTeacher(User teacher);
    void updateTeacher(User teacher);
    List<User> getPendingTeachers();
    void approveTeacher(Long teacherId);
    void rejectTeacher(Long teacherId);
    void deleteTeacher(Long teacherId);

    // --- User-related operations ---
    void createUser(User user);
    void updateUser(User user);
    void deleteUser(Long userId);

    // --- Group-related operations ---
    List<Group> getAllGroups();
    PageBean<Group> pageGroups(Integer page, Integer pageSize, String name);
    void createGroup(Group group);
    void updateGroup(Group group);
    void deleteGroup(Long groupId);

    /**
     * Get user registration trends
     */
    List<Map<String, Object>> getRegistrationTrend();

    /**
     * Get recent activities (paginated)
     */
    List<Map<String, Object>> getRecentActivities(Integer page, Integer pageSize);

    // --- Batch import operations ---

    /**
     * Batch import students from an Excel file
     *
     * @param file Excel file containing student data
     * @return import result statistics
     */
    Map<String, Object> batchImportStudents(MultipartFile file) throws Exception;

    /**
     * Batch import teachers from an Excel file
     *
     * @param file Excel file containing teacher data
     * @return import result statistics
     */
    Map<String, Object> batchImportTeachers(MultipartFile file) throws Exception;
}
