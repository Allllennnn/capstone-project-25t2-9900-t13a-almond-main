package demo.mapper;

import demo.pojo.Student;
import demo.pojo.Teacher;
import demo.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface RegisterMapper {

    /**
     * Insert into users table and return generated primary key id
     */
    @Insert("INSERT INTO users " +
            "(username, password, name, email, phone, role, status, created_at, updated_at) " +
            "VALUES(#{username}, #{password}, #{name}, #{email}, #{phone}, #{role}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(User user);

    /** Check if a username already exists */
    @Select("SELECT COUNT(1) FROM users WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    /**
     * Insert into students table
     */
    @Insert("INSERT INTO students (user_id, student_no) VALUES (#{userId}, #{studentNo})")
    void insertStudent(Student student);

    /**
     * Insert into teachers table
     */
    @Insert("INSERT INTO teachers (user_id) VALUES (#{userId})")
    void insertTeacher(Teacher teacher);
}
