package demo.mapper;

import demo.pojo.User;
import demo.pojo.Group;
import demo.pojo.Task;
import demo.pojo.TaskAssignment;
import org.apache.ibatis.annotations.*;

import java.util.List;
/**
 * Task Assignment Mapper
 */
@Mapper
public interface TaskAssignmentMapper {
    @Select("SELECT id, task_id, user_id, role, description, assigned_by, assigned_at, updated_at, status " +
            "FROM task_assignments WHERE task_id = #{taskId}")
    List<TaskAssignment> findByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT id, task_id, user_id, role, description, assigned_by, assigned_at, updated_at, status " +
            "FROM task_assignments WHERE user_id = #{userId}")
    List<TaskAssignment> findByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO task_assignments (task_id, user_id, role, description, assigned_by, assigned_at, updated_at, status) " +
            "VALUES (#{taskId}, #{userId}, #{role}, #{description}, #{assignedBy}, NOW(), NOW(), #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TaskAssignment assignment);

    @Delete("DELETE FROM task_assignments WHERE task_id = #{taskId}")
    void deleteByTaskId(@Param("taskId") Long taskId);

    @Delete("DELETE FROM task_assignments WHERE task_id = #{taskId} AND user_id = #{userId}")
    void delete(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Update("UPDATE task_assignments SET role=#{role}, description=#{description}, status=#{status}, updated_at=NOW() WHERE id=#{id}")
    void update(TaskAssignment assignment);

    @Update("UPDATE task_assignments SET status = #{status}, updated_at = NOW() WHERE task_id = #{taskId}")
    void updateStatusByTaskId(@Param("taskId") Long taskId, @Param("status") String status);

    @Select("SELECT id, task_id, user_id, role, description, assigned_by, assigned_at, updated_at, status " +
            "FROM task_assignments WHERE task_id = #{taskId} AND status = #{status}")
    List<TaskAssignment> findByTaskIdAndStatus(@Param("taskId") Long taskId, @Param("status") String status);
}
