package demo.service;

import demo.pojo.StudentRegisterRequest;
import demo.pojo.TeacherRegisterRequest;

/**
 * Registration service interface
 */
public interface RegisterService {
    /**
     * Student registration; activates the account immediately
     */
    void registerStudent(StudentRegisterRequest req);

    /**
     * Teacher registration; account status set to PENDING
     */
    void registerTeacher(TeacherRegisterRequest req);
}
