package demo.service.impl;

import demo.mapper.GroupMapper;
import demo.mapper.UserMapper;
import demo.pojo.User;
import demo.pojo.Group;
import demo.pojo.PageBean;
import demo.service.AdminService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of AdminService
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GroupMapper groupMapper;

    @Override
    public PageBean<Group> pageGroups(Integer page, Integer pageSize, String name) {
        // Set pagination
        PageHelper.startPage(page, pageSize);
        // Conditional query
        List<Group> list = groupMapper.list(name);
        Page<Group> pg = (Page<Group>) list;
        return new PageBean<>(pg.getTotal(), pg.getResult());
    }

    @Override
    public PageBean<User> pageStudents(Integer page, Integer pageSize, String name, Long id) {
        // Set pagination
        PageHelper.startPage(page, pageSize);
        // Conditional query
        List<User> list = userMapper.findStudentsByCondition(name, id);
        Page<User> pg = (Page<User>) list;
        return new PageBean<>(pg.getTotal(), pg.getResult());
    }

    @Override
    public PageBean<User> pageTeachers(Integer page, Integer pageSize, String name, Long id) {
        // Set pagination
        PageHelper.startPage(page, pageSize);
        // Conditional query
        List<User> list = userMapper.findTeachersByCondition(name, id);
        Page<User> pg = (Page<User>) list;
        return new PageBean<>(pg.getTotal(), pg.getResult());
    }

    @Override
    public List<User> getAllStudents() {
        return userMapper.findByRole("STUDENT");
    }

    @Override
    public List<User> getAllTeachers() {
        return userMapper.findByRole("TEACHER");
    }

    @Override
    public List<User> getPendingTeachers() {
        return userMapper.findByRoleAndStatus("TEACHER", "PENDING");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTeacher(Long teacherId) {
        userMapper.updateStatus(teacherId, "ACTIVE");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectTeacher(Long teacherId) {
        userMapper.updateStatus(teacherId, "REJECTED");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeacher(Long teacherId) {
        userMapper.deleteById(teacherId);
    }

    @Override
    public List<Group> getAllGroups() {
        return groupMapper.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createGroup(Group group) {
        LocalDateTime now = LocalDateTime.now();
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
        groupMapper.insert(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(Group group) {
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.update(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long groupId) {
        groupMapper.deleteById(groupId);
    }

    @Override
    public List<Map<String, Object>> getRegistrationTrend() {
        return userMapper.countUserRegistrationByDay();
    }

    @Override
    public List<Map<String, Object>> getRecentActivities(Integer page, Integer pageSize) {
        // Set pagination
        PageHelper.startPage(page, pageSize);
        return userMapper.getRecentActivities();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStudent(User student) {
        // Assign student role and default status
        student.setRole("STUDENT");
        student.setStatus("ACTIVE");
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());

        // Validate student number
        if (isEmpty(student.getStudentNo())) {
            throw new IllegalArgumentException("Student number cannot be empty");
        }
        if (isStudentNoExists(student.getStudentNo())) {
            throw new IllegalArgumentException("Student number '" + student.getStudentNo() + "' already exists");
        }

        // Save to users table
        userMapper.insert(student);
        // Save to students table
        userMapper.insertStudentInfo(student.getId(), student.getStudentNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(User student) {
        student.setUpdatedAt(LocalDateTime.now());

        // If student number is provided, check uniqueness
        if (!isEmpty(student.getStudentNo())) {
            User existing = userMapper.findById(student.getId());
            String currentNo = existing != null ?
                    userMapper.getStudentNoByUserId(existing.getId()) : null;

            if (!student.getStudentNo().equals(currentNo)) {
                if (isStudentNoExists(student.getStudentNo())) {
                    throw new IllegalArgumentException("Student number '" + student.getStudentNo() + "' already exists");
                }
                userMapper.updateStudentInfo(student.getId(), student.getStudentNo());
            }
        }

        // Update users table
        userMapper.update(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long studentId) {
        userMapper.deleteById(studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTeacher(User teacher) {
        // Assign teacher role and default status
        teacher.setRole("TEACHER");
        teacher.setStatus("ACTIVE");
        teacher.setCreatedAt(LocalDateTime.now());
        teacher.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(teacher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeacher(User teacher) {
        teacher.setUpdatedAt(LocalDateTime.now());
        userMapper.update(teacher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(User user) {
        // Set default status and timestamps
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImportStudents(MultipartFile file) throws Exception {
        Map<String, Object> result = new HashMap<>();
        int total = 0, success = 0;
        List<String> errors = new ArrayList<>();

        Workbook workbook = createWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            total++;
            Row row = sheet.getRow(i);
            if (row == null) {
                errors.add("Row " + (i + 1) + ": Empty row skipped");
                continue;
            }
            try {
                String username  = getCellValueAsString(row.getCell(0));
                String password  = getCellValueAsString(row.getCell(1));
                String name      = getCellValueAsString(row.getCell(2));
                String email     = getCellValueAsString(row.getCell(3));
                String phone     = getCellValueAsString(row.getCell(4));
                String studentNo = getCellValueAsString(row.getCell(5));

                // Validate required fields
                if (isEmpty(username) || isEmpty(password) || isEmpty(name) || isEmpty(studentNo)) {
                    errors.add("Row " + (i + 1) + ": Username, password, name, and student number are required");
                    continue;
                }

                if (isUsernameExists(username)) {
                    errors.add("Row " + (i + 1) + ": Username '" + username + "' already exists");
                    continue;
                }
                if (isStudentNoExists(studentNo)) {
                    errors.add("Row " + (i + 1) + ": Student number '" + studentNo + "' already exists");
                    continue;
                }

                User student = new User();
                student.setUsername(username);
                student.setPassword(password);
                student.setName(name);
                student.setEmail(email);
                student.setPhone(phone);
                student.setRole("STUDENT");
                student.setStatus("ACTIVE");
                student.setCreatedAt(LocalDateTime.now());
                student.setUpdatedAt(LocalDateTime.now());

                userMapper.insert(student);
                userMapper.insertStudentInfo(student.getId(), studentNo);
                success++;
            } catch (Exception e) {
                errors.add("Row " + (i + 1) + ": Import failed - " + e.getMessage());
            }
        }

        workbook.close();
        result.put("total", total);
        result.put("success", success);
        result.put("failed", total - success);
        result.put("errors", errors);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImportTeachers(MultipartFile file) throws Exception {
        Map<String, Object> result = new HashMap<>();
        int total = 0, success = 0;
        List<String> errors = new ArrayList<>();

        Workbook workbook = createWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            total++;
            Row row = sheet.getRow(i);
            if (row == null) {
                errors.add("Row " + (i + 1) + ": Empty row skipped");
                continue;
            }
            try {
                String username = getCellValueAsString(row.getCell(0));
                String password = getCellValueAsString(row.getCell(1));
                String name     = getCellValueAsString(row.getCell(2));
                String email    = getCellValueAsString(row.getCell(3));
                String phone    = getCellValueAsString(row.getCell(4));

                // Validate required fields
                if (isEmpty(username) || isEmpty(password) || isEmpty(name)) {
                    errors.add("Row " + (i + 1) + ": Username, password, and name are required");
                    continue;
                }

                if (isUsernameExists(username)) {
                    errors.add("Row " + (i + 1) + ": Username '" + username + "' already exists");
                    continue;
                }

                User teacher = new User();
                teacher.setUsername(username);
                teacher.setPassword(password);
                teacher.setName(name);
                teacher.setEmail(email);
                teacher.setPhone(phone);
                teacher.setRole("TEACHER");
                teacher.setStatus("ACTIVE");
                teacher.setCreatedAt(LocalDateTime.now());
                teacher.setUpdatedAt(LocalDateTime.now());

                userMapper.insert(teacher);
                success++;
            } catch (Exception e) {
                errors.add("Row " + (i + 1) + ": Import failed - " + e.getMessage());
            }
        }

        workbook.close();
        result.put("total", total);
        result.put("success", success);
        result.put("failed", total - success);
        result.put("errors", errors);
        return result;
    }

    /**
     * Create a Workbook based on file extension
     */
    private Workbook createWorkbook(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        InputStream is = file.getInputStream();

        if (filename.endsWith(".xlsx")) {
            return new XSSFWorkbook(is);
        } else if (filename.endsWith(".xls")) {
            return new HSSFWorkbook(is);
        } else {
            throw new Exception("Unsupported file format. Only .xlsx and .xls are supported");
        }
    }

    /**
     * Get cell value as String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /**
     * Check if a String is empty
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a username exists
     */
    private boolean isUsernameExists(String username) {
        return userMapper.findByUsername(username) != null;
    }

    /**
     * Check if a student number exists
     */
    private boolean isStudentNoExists(String studentNo) {
        return userMapper.findByStudentNo(studentNo) != null;
    }
}
