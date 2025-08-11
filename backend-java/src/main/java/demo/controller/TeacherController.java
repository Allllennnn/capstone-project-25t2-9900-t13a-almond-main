package demo.controller;

import com.github.pagehelper.PageHelper;
import demo.pojo.Result;
import demo.pojo.PageBean;
import demo.pojo.User;
import demo.pojo.Task;
import demo.service.TeacherService;
import demo.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import demo.pojo.Group;

import java.util.List;
import java.util.Map;

/**
 * Teacher API: manage students and groups
 */
@Slf4j
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private AdminService adminService;

    /** Get dashboard statistics for the teacher */
    @GetMapping("/dashboard/stats")
    public Result getDashboardStats() {
        log.info("Fetching teacher dashboard statistics");
        try {
            Map<String, Object> stats = teacherService.getDashboardStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("Failed to fetch dashboard statistics", e);
            return Result.error("Failed to fetch statistics: " + e.getMessage());
        }
    }

    /** Get recent activities for the teacher */
    @GetMapping("/recent-activities")
    public Result getRecentActivities(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("Fetching recent activities - page={}, pageSize={}", page, pageSize);
        try {
            List<Map<String, Object>> activities = teacherService.getRecentActivities(page, pageSize);
            return Result.success(activities);
        } catch (Exception e) {
            log.error("Failed to fetch recent activities", e);
            return Result.error("Failed to fetch recent activities: " + e.getMessage());
        }
    }

    /** Get the teacher’s task list */
    @GetMapping("/tasks")
    public Result getTasks(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(defaultValue = "PENDING") String status) {
        log.info("Fetching tasks - page={}, pageSize={}, status={}", page, pageSize, status);
        try {
            PageBean<Map<String, Object>> tasks = teacherService.getTasks(page, pageSize, status);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("Failed to fetch tasks", e);
            return Result.error("Failed to fetch tasks: " + e.getMessage());
        }
    }

    /** Mark a task as complete */
    @PostMapping("/tasks/{taskId}/complete")
    public Result completeTask(@PathVariable Long taskId) {
        log.info("Completing task, taskId={}", taskId);
        try {
            teacherService.completeTask(taskId);
            return Result.success("Task completed");
        } catch (Exception e) {
            log.error("Failed to complete task", e);
            return Result.error("Failed to complete task: " + e.getMessage());
        }
    }

    /** Get the teacher’s student list */
    @GetMapping("/students")
    public Result getStudents(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              String name, Long id) {
        log.info("Fetching students - page={}, pageSize={}, name={}, id={}", page, pageSize, name, id);
        try {
            PageBean<User> students = teacherService.getStudents(page, pageSize, name, id);
            return Result.success(students);
        } catch (Exception e) {
            log.error("Failed to fetch students", e);
            return Result.error("Failed to fetch students: " + e.getMessage());
        }
    }

    /** Create a new student */
    @PostMapping("/students")
    public Result createStudent(@RequestBody User student) {
        log.info("Teacher creating student: {}", student);
        try {
            adminService.createStudent(student);
            return Result.success(student);
        } catch (Exception e) {
            log.error("Failed to create student", e);
            return Result.error("Failed to create student: " + e.getMessage());
        }
    }

    /** Update an existing student */
    @PutMapping("/students/{id}")
    public Result updateStudent(@PathVariable Long id, @RequestBody User student) {
        log.info("Teacher updating student, ID={}, data={}", id, student);
        try {
            student.setId(id); // ensure correct ID
            adminService.updateStudent(student);
            return Result.success();
        } catch (Exception e) {
            log.error("Failed to update student", e);
            return Result.error("Failed to update student: " + e.getMessage());
        }
    }

    /** Get the teacher’s group list */
    @GetMapping("/groups")
    public Result getGroups(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            String name) {
        log.info("Fetching groups - page={}, pageSize={}, name={}", page, pageSize, name);
        try {
            PageBean<Group> groups = teacherService.getGroups(page, pageSize, name);
            return Result.success(groups);
        } catch (Exception e) {
            log.error("Failed to fetch groups", e);
            return Result.error("Failed to fetch groups: " + e.getMessage());
        }
    }

    /** Create a new group */
    @PostMapping("/groups")
    public Result createGroup(@RequestBody Group group) {
        log.info("Teacher creating group: {}", group);
        try {
            Group createdGroup = teacherService.createGroup(group);
            return Result.success(createdGroup);
        } catch (Exception e) {
            log.error("Failed to create group", e);
            return Result.error("Failed to create group: " + e.getMessage());
        }
    }

    /**
     * Search all students (optional fuzzy name filter) for selecting
     * members when creating a group
     */
    @GetMapping("/searchStudent")
    public Result searchStudent(@RequestParam(value = "name", required = false) String name) {
        log.info("Searching students by name: {}", name);
        List<Map<String, Object>> students = teacherService.searchStudent(name);
        return Result.success(students);
    }

    /** Batch import students */
    @PostMapping("/students/batch-import")
    public Result batchImportStudents(@RequestParam("file") MultipartFile file) {
        log.info("Teacher batch importing students, fileName={}", file.getOriginalFilename());
        try {
            Map<String, Object> result = adminService.batchImportStudents(file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to batch import students", e);
            return Result.error("Failed to batch import students: " + e.getMessage());
        }
    }

    /** Batch import groups */
    @PostMapping("/groups/batch-import")
    public Result batchImportGroups(@RequestParam("file") MultipartFile file,
                                    @RequestAttribute("userId") Long teacherId) {
        log.info("Teacher batch importing groups, fileName={}", file.getOriginalFilename());
        try {
            Map<String, Object> result = teacherService.batchImportGroups(file, teacherId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to batch import groups", e);
            return Result.error("Failed to batch import groups: " + e.getMessage());
        }
    }

    /** Batch import tasks */
    @PostMapping("/tasks/batch-import")
    public Result batchImportTasks(@RequestParam("file") MultipartFile file,
                                   @RequestAttribute("userId") Long teacherId) {
        log.info("Teacher batch importing tasks, fileName={}", file.getOriginalFilename());
        try {
            Map<String, Object> result = teacherService.batchImportTasks(file, teacherId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to batch import tasks", e);
            return Result.error("Failed to batch import tasks: " + e.getMessage());
        }
    }
}
