package demo.mapper;

import demo.pojo.User;
import demo.pojo.PageBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TeacherMapper {

    /**
     * Paginate query of users with role TEACHER, supporting fuzzy search by name and exact match by id
     */
    List<User> list(String name, Long id);

    /**
     * Paginate query of students (users with role STUDENT)
     */
    PageBean<User> page(@Param("page") Integer page,
                        @Param("pageSize") Integer pageSize,
                        @Param("name") String name,
                        @Param("id") Long id);
}
