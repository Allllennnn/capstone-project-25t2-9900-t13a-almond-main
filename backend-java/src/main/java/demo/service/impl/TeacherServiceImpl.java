package demo.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import demo.mapper.GroupMapper;
import demo.mapper.StudentMapper;
import demo.mapper.TeacherMapper;
import demo.mapper.UserGroupMapper;
import demo.mapper.UserMapper;
import demo.mapper.TaskMapper;
import demo.pojo.Group;
import demo.pojo.PageBean;
import demo.pojo.User;
import demo.pojo.Task;
import demo.service.TeacherService;
import demo.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Teacher service implementation class
 */
@Slf4j
@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get total number of students
        Long totalStudents = userMapper.countByRole("STUDENT");
        stats.put("totalStudents", totalStudents);
        
        // Get total number of groups
        Long totalGroups = groupMapper.count();
        stats.put("totalGroups", totalGroups);
        
        // Simulated active tasks count
        stats.put("activeTasks", 5);
        
        // Simulated completed tasks count
        stats.put("completedTasks", 12);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getRecentActivities(Integer page, Integer pageSize) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Simulate recent activity data
        String[] activityTypes = {"student_joined", "assignment_submitted", "group_created", "task_completed"};
        String[] activityContents = {
                "A new student has joined your group",
                "A group assignment was submitted",
                "A new group was created",
                "A task was completed"
        };
        
        int offset = (page - 1) * pageSize;
        for (int i = 0; i < pageSize && i < 10; i++) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", offset + i + 1);
            activity.put("type", activityTypes[i % activityTypes.length]);
            activity.put("content", activityContents[i % activityContents.length]);
            LocalDateTime time = LocalDateTime.now().minusHours(i + 1);
            activity.put("time", time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            activity.put("timestamp", time.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
            activities.add(activity);
        }
        
        return activities;
    }

    @Override
    public PageBean<Map<String, Object>> getTasks(Integer page, Integer pageSize, String status) {
        // Simulate paginated task data
        List<Map<String, Object>> tasks = new ArrayList<>();
        String[] titles = {"Review Final Projects", "Prepare Midterm Exam", "Grade Assignments", "Update Course Materials"};
        String[] courses = {"CS301 - Algorithms", "CS201 - Data Structures", "CS101 - Programming", "CS401 - Advanced Topics"};
        String[] priorities = {"high", "medium", "low"};
        
        int offset = (page - 1) * pageSize;
        for (int i = 0; i < pageSize && i < 8; i++) {
            Map<String, Object> task = new HashMap<>();
            task.put("id", offset + i + 1);
            task.put("title", titles[i % titles.length]);
            task.put("course", courses[i % courses.length]);
            task.put("description", "Detailed description of task " + (i + 1));
            task.put("priority", priorities[i % priorities.length]);
            task.put("status", status);
            task.put("dueDate", LocalDateTime.now().plusDays(i + 1)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            tasks.add(task);
        }
        
        return new PageBean<>(8L, tasks, page, pageSize);
    }

    @Override
    public void completeTask(Long taskId) {
        // Simulate marking the task as completed
        log.info("Completing task: {}", taskId);
        // In a real implementation, update the task status in the database
    }

    @Override
    public PageBean<User> getStudents(Integer page, Integer pageSize, String name, Long id) {
        PageHelper.startPage(page, pageSize);
        List<User> list = teacherMapper.list(name, id);
        Page<User> pg = (Page<User>) list;
        return new PageBean<>(pg.getTotal(), pg.getResult(), page, pageSize);
    }

    @Override
    public PageBean<Group> getGroups(Integer page, Integer pageSize, String name) {
        PageHelper.startPage(page, pageSize);
        List<Group> list = groupMapper.list(name);
        Page<Group> pg = (Page<Group>) list;
        return new PageBean<>(pg.getTotal(), pg.getResult(), page, pageSize);
    }

    @Override
    public Group createGroup(Group group) {
        // Set creation and update timestamps
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        
        // Extract the current teacher ID from the JWT token
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                try {
                    Map<String, Object> claims = JwtUtils.parseJwt(token);
                    Long teacherId = Long.valueOf(claims.get("userId").toString());
                    group.setTeacherId(teacherId);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to extract teacher ID from token");
                }
            }
        }
        
        // Ensure teacherId is set
        if (group.getTeacherId() == null) {
            throw new RuntimeException("Teacher ID is required");
        }
        
        // Insert the new group
        groupMapper.insert(group);

        // Associate members if provided
        if (group.getMembers() != null && !group.getMembers().isEmpty()) {
            userGroupMapper.batchInsert(group.getId(), group.getMembers());
        }
        
        return group;
    }

    @Override
    public List<Map<String, Object>> searchStudent(String name) {
        return studentMapper.searchStudent(name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImportGroups(MultipartFile file, Long teacherId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = createWorkbook(is, file.getOriginalFilename());
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name      = getCellValueAsString(row.getCell(0));
                    String desc      = getCellValueAsString(row.getCell(1));
                    String membersStr = getCellValueAsString(row.getCell(2));

                    if (isEmpty(name)) {
                        errors.add("Row " + (i + 1) + ": group name is required");
                        continue;
                    }

                    if (groupMapper.findByName(name) != null) {
                        errors.add("Row " + (i + 1) + ": group '" + name + "' already exists");
                        continue;
                    }

                    Group group = new Group();
                    group.setName(name);
                    group.setDescription(desc);
                    group.setTeacherId(teacherId);
                    group.setCreatedAt(LocalDateTime.now());
                    group.setUpdatedAt(LocalDateTime.now());
                    groupMapper.insert(group);

                    List<Long> memberIds = parseMembers(membersStr);
                    List<Long> valid = new ArrayList<>();
                    List<String> invalid = new ArrayList<>();

                    for (Long sid : memberIds) {
                        User u = userMapper.findById(sid);
                        if (u != null && "STUDENT".equals(u.getRole())) {
                            valid.add(sid);
                        } else {
                            invalid.add(String.valueOf(sid));
                        }
                    }

                    if (!invalid.isEmpty()) {
                        errors.add("Row " + (i + 1) + ": invalid student IDs: " +
                                String.join(", ", invalid));
                    }

                    if (!valid.isEmpty()) {
                        userGroupMapper.batchInsert(group.getId(), valid);
                    } else if (!memberIds.isEmpty()) {
                        groupMapper.deleteById(group.getId());
                        errors.add("Row " + (i + 1) + ": no valid student IDs, group creation cancelled");
                        continue;
                    }

                    successCount++;
                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": import failed – " + e.getMessage());
                    log.error("Import failed at row {}", i + 1, e);
                }
            }
        }

        result.put("success", successCount);
        result.put("errors", errors);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImportTasks(MultipartFile file, Long teacherId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = createWorkbook(is, file.getOriginalFilename());
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String title      = getCellValueAsString(row.getCell(0));
                    String desc       = getCellValueAsString(row.getCell(1));
                    String groupIdStr = getCellValueAsString(row.getCell(2));
                    String dueDateStr = getCellValueAsString(row.getCell(3));
                    String cycleStr   = getCellValueAsString(row.getCell(4));
                    String fileUrl    = getCellValueAsString(row.getCell(5));

                    if (isEmpty(title)) {
                        errors.add("Row " + (i + 1) + ": task title is required");
                        continue;
                    }
                    if (isEmpty(groupIdStr)) {
                        errors.add("Row " + (i + 1) + ": group ID is required");
                        continue;
                    }

                    Long groupId;
                    try {
                        groupId = Long.valueOf(groupIdStr);
                    } catch (NumberFormatException e) {
                        errors.add("Row " + (i + 1) + ": invalid group ID format");
                        continue;
                    }

                    Group group = groupMapper.findById(groupId);
                    if (group == null) {
                        errors.add("Row " + (i + 1) + ": group ID " + groupId + " not found");
                        continue;
                    }
                    if (!group.getTeacherId().equals(teacherId)) {
                        errors.add("Row " + (i + 1) + ": group ID " + groupId +
                                " does not belong to current teacher");
                        continue;
                    }

                    Task task = new Task();
                    task.setTitle(title);
                    task.setDescription(desc);
                    task.setGroupId(groupId);
                    task.setStatus("INITIALIZING");
                    task.setFileUrl(fileUrl);

                    if (!isEmpty(dueDateStr)) {
                        try {
                            task.setDueDate(LocalDate.parse(dueDateStr));
                        } catch (Exception e) {
                            errors.add("Row " + (i + 1) + ": invalid due date format (YYYY-MM-DD)");
                            continue;
                        }
                    }

                    if (!isEmpty(cycleStr)) {
                        try {
                            task.setCycle(Integer.parseInt(cycleStr));
                        } catch (NumberFormatException e) {
                            errors.add("Row " + (i + 1) + ": invalid cycle format");
                            continue;
                        }
                    } else {
                        task.setCycle(1);
                    }

                    task.setCreatedAt(LocalDateTime.now());
                    task.setUpdatedAt(LocalDateTime.now());
                    taskMapper.insert(task);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": import failed – " + e.getMessage());
                    log.error("Import failed at row {}", i + 1, e);
                }
            }
        }

        result.put("success", successCount);
        result.put("errors", errors);
        return result;
    }

    // Helper to parse member ID string "1,2,3"
    private List<Long> parseMembers(String membersStr) {
        if (isEmpty(membersStr)) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>();
        try {
            for (String part : membersStr.split(",")) {
                part = part.trim();
                if (!part.isEmpty()) {
                    ids.add(Long.valueOf(part));
                }
            }
        } catch (NumberFormatException e) {
            log.error("Failed to parse member IDs", e);
        }
        return ids;
    }

    // Helper to create a Workbook from an InputStream
    private Workbook createWorkbook(InputStream is, String filename) throws Exception {
        if (filename.endsWith(".xlsx")) {
            return new XSSFWorkbook(is);
        }
        throw new IllegalArgumentException("Unsupported file format, please use .xlsx");
    }

    // Helper to get cell value as String
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    // Helper to check if a string is empty
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
