package demo.service.impl;

import demo.mapper.RegisterMapper;
import demo.pojo.Student;
import demo.pojo.StudentRegisterRequest;
import demo.pojo.Teacher;
import demo.pojo.TeacherRegisterRequest;
import demo.pojo.User;
import demo.service.RegisterService;
import demo.exception.UsernameExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private RegisterMapper registerMapper;

    /**
     * Student registration: activates the account immediately upon successful registration
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerStudent(StudentRegisterRequest req) {
        // Validate unique username
        if (registerMapper.countByUsername(req.getUsername()) > 0) {
            throw new UsernameExistsException("Username already exists");
        }
        // 1. Prepare User object
        User user = new User();
        user.setUsername(req.getUsername());
        // MD5 encrypt password (can switch to BCrypt)
        // user.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        user.setPassword(req.getPassword());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole("STUDENT");
        user.setStatus("ACTIVE");
        // Insert into users table and retrieve generated user ID
        registerMapper.insertUser(user);

        // 2. Insert into students table
        Student student = new Student();
        student.setUserId(user.getId());
        student.setStudentNo(req.getStudentNo());
        registerMapper.insertStudent(student);
    }

    /**
     * Teacher registration: sets the account to PENDING and requires admin approval
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerTeacher(TeacherRegisterRequest req) {
        // Validate unique username
        if (registerMapper.countByUsername(req.getUsername()) > 0) {
            throw new UsernameExistsException("Username already exists");
        }
        // 1. Prepare User object
        User user = new User();
        user.setUsername(req.getUsername());
        // user.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        user.setPassword(req.getPassword());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole("TEACHER");
        user.setStatus("PENDING");
        // Insert into users table
        registerMapper.insertUser(user);

        // 2. Insert into teachers table
        Teacher teacher = new Teacher();
        teacher.setUserId(user.getId());
        registerMapper.insertTeacher(teacher);
    }
}
