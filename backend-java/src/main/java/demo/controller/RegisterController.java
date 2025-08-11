package demo.controller;

import demo.pojo.Result;
import demo.pojo.StudentRegisterRequest;
import demo.pojo.TeacherRegisterRequest;
import demo.service.RegisterService;
import demo.exception.UsernameExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    /**
     * Student registration: new student accounts are activated immediately
     */
    @PostMapping("/student")
    public Result registerStudent(@RequestBody StudentRegisterRequest req) {
        log.info("Student registration request: {}", req);
        try {
            registerService.registerStudent(req);
            return Result.success("Registration successful");
        } catch (UsernameExistsException e) {
            return Result.error("Username already exists");
        }
    }

    /**
     * Teacher registration: new teacher accounts are set to PENDING and require admin approval
     */
    @PostMapping("/teacher")
    public Result registerTeacher(@RequestBody TeacherRegisterRequest req) {
        log.info("Teacher registration request: {}", req);
        try {
            registerService.registerTeacher(req);
            return Result.success("Registration successful, pending admin approval");
        } catch (UsernameExistsException e) {
            return Result.error("Username already exists");
        }
    }
}
