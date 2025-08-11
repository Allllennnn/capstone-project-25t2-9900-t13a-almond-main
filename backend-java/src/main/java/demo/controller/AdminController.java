package demo.controller;

import demo.pojo.Group;
import demo.pojo.Result;
import demo.pojo.User;
import demo.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import demo.pojo.PageBean;

import java.util.List;
import java.util.Map;

/**
 * Administrator API: CRUD operations for teachers, students, and groups,
 * plus teacher approval workflows
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /** 1. Paginate and retrieve all students */
    @GetMapping("/students")
    public Result pageStudents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String name,
            Long id) {
        log.info("Admin listing students - page={}, pageSize={}, name={}, id={}",
                page, pageSize, name, id);
        PageBean<User> pageBean = adminService.pageStudents(page, pageSize, name, id);
        return Result.success(pageBean);
    }

    /** 1.1. Create a new student */
    @PostMapping("/students")
    public Result createStudent(@RequestBody User student) {
        log.info("Admin creating student: {}", student);
        adminService.createStudent(student);
        return Result.success(student);
    }

    /** 1.2. Update an existing student */
    @PutMapping("/students")
    public Result updateStudent(@RequestBody User student) {
        log.info("Admin updating student: {}", student);
        adminService.updateStudent(student);
        return Result.success();
    }

    /** 1.3. Delete a student by ID */
    @DeleteMapping("/students/{id}")
    public Result deleteStudent(@PathVariable Long id) {
        log.info("Admin deleting student, ID={}", id);
        adminService.deleteStudent(id);
        return Result.success("Student deleted");
    }

    /** 2. Paginate and retrieve all teachers */
    @GetMapping("/teachers")
    public Result pageTeachers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String name,
            Long id) {
        log.info("Admin listing teachers - page={}, pageSize={}, name={}, id={}",
                page, pageSize, name, id);
        PageBean<User> pageBean = adminService.pageTeachers(page, pageSize, name, id);
        return Result.success(pageBean);
    }

    /** 2.1. Create a new teacher */
    @PostMapping("/teachers")
    public Result createTeacher(@RequestBody User teacher) {
        log.info("Admin creating teacher: {}", teacher);
        adminService.createTeacher(teacher);
        return Result.success(teacher);
    }

    /** 2.2. Update an existing teacher */
    @PutMapping("/teachers")
    public Result updateTeacher(@RequestBody User teacher) {
        log.info("Admin updating teacher: {}", teacher);
        adminService.updateTeacher(teacher);
        return Result.success();
    }

    /** 3. List teachers pending approval */
    @GetMapping("/teachers/pending")
    public Result listPendingTeachers() {
        log.info("Admin retrieving pending teacher approvals");
        List<User> pending = adminService.getPendingTeachers();
        return Result.success(pending);
    }

    /** 4. Approve a teacher registration */
    @PostMapping("/teachers/{id}/approve")
    public Result approveTeacher(@PathVariable Long id) {
        log.info("Admin approving teacher registration, ID={}", id);
        adminService.approveTeacher(id);
        return Result.success("Teacher approved");
    }

    /** 4.1. Reject a teacher registration */
    @PostMapping("/teachers/{id}/reject")
    public Result rejectTeacher(@PathVariable Long id) {
        log.info("Admin rejecting teacher registration, ID={}", id);
        adminService.rejectTeacher(id);
        return Result.success("Teacher registration rejected");
    }

    /** 5. Delete a teacher by ID */
    @DeleteMapping("/teachers/{id}")
    public Result deleteTeacher(@PathVariable Long id) {
        log.info("Admin deleting teacher, ID={}", id);
        adminService.deleteTeacher(id);
        return Result.success("Teacher deleted");
    }

    /** 6. Create a generic user */
    @PostMapping("/users")
    public Result createUser(@RequestBody User user) {
        log.info("Admin creating user: {}", user);
        adminService.createUser(user);
        return Result.success(user);
    }

    /** 7. Update a generic user */
    @PutMapping("/users")
    public Result updateUser(@RequestBody User user) {
        log.info("Admin updating user: {}", user);
        adminService.updateUser(user);
        return Result.success();
    }

    /** 8. Delete a generic user by ID */
    @DeleteMapping("/users/{id}")
    public Result deleteUser(@PathVariable Long id) {
        log.info("Admin deleting user, ID={}", id);
        adminService.deleteUser(id);
        return Result.success("User deleted");
    }

    /** 9. Paginate and retrieve groups */
    @GetMapping("/groups")
    public Result pageGroups(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String name) {
        log.info("Admin listing groups - page={}, pageSize={}, name={}",
                page, pageSize, name);
        PageBean<Group> pageBean = adminService.pageGroups(page, pageSize, name);
        return Result.success(pageBean);
    }

    /** 9.1. Create a new group */
    @PostMapping("/groups")
    public Result createGroup(@RequestBody Group group) {
        log.info("Admin creating group: {}", group);
        adminService.createGroup(group);
        return Result.success(group);
    }

    /** 9.2. Update an existing group */
    @PutMapping("/groups")
    public Result updateGroup(@RequestBody Group group) {
        log.info("Admin updating group: {}", group);
        adminService.updateGroup(group);
        return Result.success();
    }

    /** 9.3. Delete a group by ID */
    @DeleteMapping("/groups/{id}")
    public Result deleteGroup(@PathVariable Long id) {
        log.info("Admin deleting group, ID={}", id);
        adminService.deleteGroup(id);
        return Result.success("Group deleted");
    }

    /** 10. Retrieve registration trend data */
    @GetMapping("/registration-trend")
    public Result getRegistrationTrend() {
        log.info("Starting to fetch registration trend data");
        try {
            List<Map<String, Object>> trend = adminService.getRegistrationTrend();
            log.info("Successfully fetched registration trend data, count={}", trend.size());
            return Result.success(trend);
        } catch (Exception e) {
            log.error("Failed to fetch registration trend data", e);
            return Result.error("Failed to fetch registration trend data: " + e.getMessage());
        }
    }

    /** 11. Retrieve recent activities */
    @GetMapping("/recent-activities")
    public Result getRecentActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("Fetching recent activities - page={}, pageSize={}", page, pageSize);
        try {
            List<Map<String, Object>> activities = adminService.getRecentActivities(page, pageSize);
            return Result.success(activities);
        } catch (Exception e) {
            log.error("Failed to fetch recent activities", e);
            return Result.error("Failed to fetch recent activities: " + e.getMessage());
        }
    }

    /** 12. Batch import students from file */
    @PostMapping("/students/batch-import")
    public Result batchImportStudents(@RequestParam("file") MultipartFile file) {
        log.info("Admin batch importing students, fileName={}", file.getOriginalFilename());
        try {
            Map<String, Object> result = adminService.batchImportStudents(file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to batch import students", e);
            return Result.error("Failed to batch import students: " + e.getMessage());
        }
    }

    /** 13. Batch import teachers from file */
    @PostMapping("/teachers/batch-import")
    public Result batchImportTeachers(@RequestParam("file") MultipartFile file) {
        log.info("Admin batch importing teachers, fileName={}", file.getOriginalFilename());
        try {
            Map<String, Object> result = adminService.batchImportTeachers(file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to batch import teachers", e);
            return Result.error("Failed to batch import teachers: " + e.getMessage());
        }
    }
}
