package demo.mapper;

import demo.pojo.MemberWeeklyGoal;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * MemberWeeklyGoal Mapper
 */
@Mapper
public interface MemberWeeklyGoalMapper {

    @Insert("INSERT INTO member_weekly_goals (task_id, student_id, week_no, goal, status, created_at, updated_at) " +
            "VALUES (#{taskId}, #{studentId}, #{weekNo}, #{goal}, #{status}, NOW(), NOW())")
    void insert(MemberWeeklyGoal goal);

    @Update("UPDATE member_weekly_goals SET goal=#{goal}, status=#{status}, updated_at=NOW() WHERE id=#{id}")
    void update(MemberWeeklyGoal goal);

    @Select("SELECT * FROM member_weekly_goals WHERE task_id=#{taskId} AND student_id=#{studentId}")
    List<MemberWeeklyGoal> findByTaskAndStudent(@Param("taskId") Long taskId,
                                                @Param("studentId") Long studentId);

    @Select("SELECT * FROM member_weekly_goals WHERE task_id=#{taskId}")
    List<MemberWeeklyGoal> findByTaskId(@Param("taskId") Long taskId);

    @Delete("DELETE FROM member_weekly_goals WHERE id=#{id}")
    void delete(@Param("id") Long id);

    @Select("SELECT * FROM member_weekly_goals WHERE id=#{id}")
    MemberWeeklyGoal findById(@Param("id") Long id);

    /** Get goals by task ID and week number */
    @Select("SELECT * FROM member_weekly_goals WHERE task_id=#{taskId} AND week_no=#{weekNo}")
    List<MemberWeeklyGoal> findByTaskIdAndWeekNo(@Param("taskId") Long taskId,
                                                 @Param("weekNo") Integer weekNo);

    /** Update the status of a weekly goal */
    @Update("UPDATE member_weekly_goals SET status=#{status}, updated_at=NOW() WHERE id=#{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    /** Get all weekly goals by task ID and student ID, ordered by week number */
    @Select("SELECT * FROM member_weekly_goals WHERE task_id=#{taskId} AND student_id=#{studentId} ORDER BY week_no ASC")
    List<MemberWeeklyGoal> findByTaskIdAndStudentId(@Param("taskId") Long taskId,
                                                    @Param("studentId") Long studentId);
}
