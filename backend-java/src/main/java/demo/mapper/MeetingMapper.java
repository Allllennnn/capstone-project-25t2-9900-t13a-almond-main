package demo.mapper;

import demo.pojo.Meeting;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Meeting Mapper
 */
@Mapper
public interface MeetingMapper {
    
    @Select("SELECT * FROM meetings WHERE task_id = #{taskId} ORDER BY meeting_no")
    List<Meeting> findByTaskId(@Param("taskId") Long taskId);
    
    @Select("SELECT * FROM meetings WHERE group_id = #{groupId} AND task_id = #{taskId} ORDER BY meeting_no")
    List<Meeting> findByGroupIdAndTaskId(@Param("groupId") Long groupId, @Param("taskId") Long taskId);
    
    @Select("SELECT * FROM meetings WHERE id = #{id}")
    Meeting findById(@Param("id") Long id);
    
    @Select("SELECT * FROM meetings WHERE task_id = #{taskId} AND meeting_no = #{meetingNo}")
    Meeting findByTaskIdAndMeetingNo(@Param("taskId") Long taskId, @Param("meetingNo") Integer meetingNo);
    
    @Insert("INSERT INTO meetings (group_id, task_id, meeting_no, meeting_date, status, document_url, created_at, updated_at) " +
            "VALUES (#{groupId}, #{taskId}, #{meetingNo}, #{meetingDate}, #{status}, #{documentUrl}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Meeting meeting);
    
    @Update("UPDATE meetings SET status = #{status}, document_url = #{documentUrl}, updated_at = NOW() WHERE id = #{id}")
    void update(Meeting meeting);
    
    @Update("UPDATE meetings SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Delete("DELETE FROM meetings WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
    
    @Select("SELECT COUNT(*) FROM meetings WHERE task_id = #{taskId} AND meeting_no < #{meetingNo} AND status = 'UNFINISHED'")
    int countUnfinishedPreviousMeetings(@Param("taskId") Long taskId, @Param("meetingNo") Integer meetingNo);
    
    @Select("SELECT MAX(meeting_no) FROM meetings WHERE task_id = #{taskId}")
    Integer findMaxMeetingNoByTaskId(@Param("taskId") Long taskId);
} 