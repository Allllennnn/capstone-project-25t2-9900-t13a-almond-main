package demo.mapper;

import demo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper {
    /**
     * Paginate query of users with role STUDENT, supporting fuzzy search by name and exact match by id
     */
    List<User> list(String name, Long id);

    /**
     * Search students for group creation (optional fuzzy name search)
     */
    List<Map<String, Object>> searchStudent(String name);
}
