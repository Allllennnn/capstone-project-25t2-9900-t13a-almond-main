package demo.mapper;

import demo.pojo.Group;
import demo.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserGroupMapper {

    /**
     * Find the group ID for a given user ID
     */
    @Select("SELECT group_id FROM user_group WHERE user_id = #{userId}")
    Long findGroupIdByUserId(@Param("userId") Long userId);

    /**
     * Find all users in a given group
     */
    @Select("SELECT u.* FROM users u " +
            "JOIN user_group ug ON u.id = ug.user_id " +
            "WHERE ug.group_id = #{groupId}")
    List<User> findMembersByGroupId(@Param("groupId") Long groupId);

    /**
     * Remove a user from a group
     */
    @Delete("DELETE FROM user_group WHERE user_id = #{userId} AND group_id = #{groupId}")
    void delete(@Param("userId") Long userId, @Param("groupId") Long groupId);

    /**
     * Batch insert user-group associations
     */
    @Insert({
            "<script>",
            "INSERT INTO user_group (user_id, group_id, assigned_at) VALUES ",
            "<foreach collection='userIds' item='userId' separator=','>",
            "(#{userId}, #{groupId}, NOW())",
            "</foreach>",
            "</script>"
    })
    void batchInsert(@Param("groupId") Long groupId, @Param("userIds") List<Long> userIds);

    /**
     * Retrieve all groups a user participates in
     */
    @Select("SELECT g.* FROM `groups` g " +
            "JOIN user_group ug ON g.id = ug.group_id " +
            "WHERE ug.user_id = #{userId}")
    List<Group> findGroupsByUserId(@Param("userId") Long userId);

    /**
     * Retrieve all group IDs a user participates in
     */
    @Select("SELECT group_id FROM user_group WHERE user_id = #{userId}")
    List<Long> findGroupIdsByUserId(@Param("userId") Long userId);

    /**
     * Find all members of the group associated with a given task
     */
    @Select("SELECT u.* FROM users u " +
            "JOIN user_group ug ON u.id = ug.user_id " +
            "JOIN tasks t ON t.group_id = ug.group_id " +
            "WHERE t.id = #{taskId}")
    List<User> findMembersByTaskId(@Param("taskId") Long taskId);

    /**
     * Find the group ID associated with a given task
     */
    @Select("SELECT t.group_id FROM tasks t WHERE t.id = #{taskId}")
    Long findGroupIdByTaskId(@Param("taskId") Long taskId);
}
