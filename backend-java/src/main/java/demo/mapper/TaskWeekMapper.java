package demo.mapper;

import demo.pojo.TaskWeek;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Task Weekly Goal Mapper
 */
@Mapper
public interface TaskWeekMapper {
    
    @Insert("INSERT INTO task_weeks (task_id, week_no, goal, created_at, updated_at) VALUES (#{taskId}, #{weekNo}, #{goal}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TaskWeek taskWeek);

    @Update("UPDATE task_weeks SET goal=#{goal}, updated_at=NOW() WHERE id=#{id}")
    void update(TaskWeek taskWeek);

    @Select("SELECT id, task_id, week_no, goal, created_at, updated_at FROM task_weeks WHERE task_id=#{taskId}")
    List<TaskWeek> findByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT id, task_id, week_no, goal, created_at, updated_at FROM task_weeks WHERE task_id=#{taskId} AND week_no=#{weekNo}")
    TaskWeek findByTaskIdAndWeekNo(@Param("taskId") Long taskId, @Param("weekNo") Integer weekNo);

    @Delete("DELETE FROM task_weeks WHERE id=#{id}")
    void delete(@Param("id") Long id);

    @Delete("DELETE FROM task_weeks WHERE task_id=#{taskId}")
    void deleteByTaskId(@Param("taskId") Long taskId);
} 