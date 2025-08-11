package demo.service;

import demo.pojo.PageBean;
import demo.pojo.User;
import demo.pojo.Group;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Teacher service interface
 */
public interface TeacherService {
    /**
     * Retrieve statistics for the teacher dashboard
     */
    Map<String, Object> getDashboardStats();

    /**
     * Retrieve the teacher's recent activities (paginated)
     *
     * @param page     page number
     * @param pageSize number of items per page
     * @return list of activity maps
     */
    List<Map<String, Object>> getRecentActivities(Integer page, Integer pageSize);

    /**
     * Retrieve the teacher's tasks (paginated) filtered by status
     *
     * @param page     page number
     * @param pageSize number of items per page
     * @param status   task status filter
     * @return paginated tasks
     */
    PageBean<Map<String, Object>> getTasks(Integer page, Integer pageSize, String status);

    /**
     * Mark a task as completed
     *
     * @param taskId ID of the task to complete
     */
    void completeTask(Long taskId);

    /**
     * Retrieve the teacher's student list (paginated) with optional filters
     *
     * @param page     page number
     * @param pageSize number of items per page
     * @param name     optional student name filter
     * @param id       optional student ID filter
     * @return paginated students
     */
    PageBean<User> getStudents(Integer page, Integer pageSize, String name, Long id);

    /**
     * Retrieve the teacher's group list (paginated) with optional name filter
     *
     * @param page     page number
     * @param pageSize number of items per page
     * @param name     optional group name filter
     * @return paginated groups
     */
    PageBean<Group> getGroups(Integer page, Integer pageSize, String name);

    /**
     * Create a new group
     *
     * @param group Group object to create
     * @return the created Group
     */
    Group createGroup(Group group);

    /**
     * Search students by name for group creation (optional fuzzy search)
     *
     * @param name optional student name filter
     * @return list of student maps
     */
    List<Map<String, Object>> searchStudent(String name);

    /**
     * Batch import groups from an Excel file
     *
     * @param file      Excel file containing group data
     * @param teacherId ID of the teacher performing the import
     * @return import result statistics
     * @throws Exception on import errors
     */
    Map<String, Object> batchImportGroups(MultipartFile file, Long teacherId) throws Exception;

    /**
     * Batch import tasks from an Excel file
     *
     * @param file      Excel file containing task data
     * @param teacherId ID of the teacher performing the import
     * @return import result statistics
     * @throws Exception on import errors
     */
    Map<String, Object> batchImportTasks(MultipartFile file, Long teacherId) throws Exception;
}
