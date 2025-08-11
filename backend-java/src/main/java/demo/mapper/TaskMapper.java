package demo.mapper;

import demo.pojo.User;
import demo.pojo.Group;
import demo.pojo.Task;
import demo.pojo.TaskAssignment;
import org.apache.ibatis.annotations.*;

import java.util.List;
/**
 * Task Mapper
 */
@Mapper
public interface TaskMapper {
    @Select("SELECT id, title, description, group_id, status, due_date, cycle, created_at, updated_at, file_url FROM tasks WHERE group_id = #{groupId}")
    List<Task> findByGroupId(@Param("groupId") Long groupId);

    @Select("SELECT id, title, description, group_id, status, due_date, cycle, created_at, updated_at, file_url FROM tasks WHERE id = #{id}")
    Task findById(@Param("id") Long id);

    @Insert("INSERT INTO tasks (title, description, group_id, status, due_date, cycle, file_url, created_at, updated_at) VALUES (#{title}, #{description}, #{groupId}, #{status}, #{dueDate}, #{cycle}, #{fileUrl}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Task task);

    @Update("UPDATE tasks SET title=#{title}, description=#{description}, status=#{status}, due_date=#{dueDate}, cycle=#{cycle}, file_url=#{fileUrl}, updated_at=NOW() WHERE id=#{id}")
    void update(Task task);

    @Delete("DELETE FROM tasks WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}