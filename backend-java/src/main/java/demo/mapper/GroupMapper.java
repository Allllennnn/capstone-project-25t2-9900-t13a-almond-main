package demo.mapper;

import demo.pojo.Group;
import demo.pojo.PageBean;
import demo.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Group Mapper
 */
@Mapper
public interface GroupMapper {

    /**
     * Conditional query for groups, supports fuzzy search by name
     */
    List<Group> list(@Param("name") String name);

    // Keep the method for querying all records without conditions (if still in use)
    List<Group> findAll();

    @Insert("INSERT INTO `groups` (name, description, teacher_id, created_at, updated_at) " +
            "VALUES (#{name}, #{description}, #{teacherId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Group group);

    @Update("UPDATE `groups` SET name=#{name}, description=#{description}, " +
            "teacher_id=#{teacherId}, updated_at=NOW() WHERE id=#{id}")
    void update(Group group);

    @Delete("DELETE FROM `groups` WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Select("SELECT * FROM `groups` WHERE id = #{id}")
    Group findById(@Param("id") Long id);

    // Count total number of groups
    @Select("SELECT COUNT(*) FROM `groups`")
    Long count();

    // Paginate groups query
    PageBean<Group> page(@Param("page") Integer page,
                         @Param("pageSize") Integer pageSize,
                         @Param("name") String name);

    @Select("SELECT u.* FROM users u JOIN user_group ug ON u.id = ug.user_id WHERE ug.group_id = #{groupId}")
    List<User> findGroupMembers(@Param("groupId") Long groupId);

    // Query group by name
    @Select("SELECT * FROM `groups` WHERE name = #{name} LIMIT 1")
    Group findByName(@Param("name") String name);
}
