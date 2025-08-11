package demo.mapper;

import demo.pojo.Conversation;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ConversationMapper {
    
    @Insert("INSERT INTO conversations (task_id, group_id, sender_id, sender_type, content) " +
            "VALUES (#{taskId}, #{groupId}, #{senderId}, #{senderType}, #{content})")
    void insert(Conversation conversation);
    
    @Select("SELECT c.id, c.task_id, c.group_id, c.sender_id, c.sender_type, c.content, c.created_at, " +
            "CASE " +
            "    WHEN c.sender_type = 'AGENT' THEN 'AI Assistant' " +
            "    WHEN c.sender_type = 'USER' AND u.name IS NOT NULL THEN u.name " +
            "    WHEN c.sender_type = 'USER' AND u.name IS NULL THEN 'Unknown User' " +
            "    ELSE 'Unknown' " +
            "END as sender_name " +
            "FROM conversations c " +
            "LEFT JOIN users u ON c.sender_id = u.id " +
            "WHERE c.task_id = #{taskId} AND c.group_id = #{groupId} " +
            "ORDER BY c.created_at ASC")
    List<Map<String, Object>> findByTaskIdAndGroupId(@Param("taskId") Long taskId, @Param("groupId") Long groupId);
    
    @Delete("DELETE FROM conversations WHERE task_id = #{taskId} AND group_id = #{groupId}")
    void deleteByTaskIdAndGroupId(@Param("taskId") Long taskId, @Param("groupId") Long groupId);

    @Select("SELECT c.id, c.task_id, c.group_id, c.sender_id, c.sender_type, c.content, c.created_at " +
            "FROM conversations c " +
            "WHERE c.task_id = #{taskId} AND c.group_id = #{groupId} " +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> findRecentByTaskAndGroup(@Param("taskId") Long taskId, 
                                                      @Param("groupId") Long groupId, 
                                                      @Param("limit") int limit);
} 