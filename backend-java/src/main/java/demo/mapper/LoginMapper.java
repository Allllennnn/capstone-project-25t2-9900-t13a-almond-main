package demo.mapper;

import demo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginMapper {

    /**
     * Verify that username + password + role match
     */
    @Select("SELECT * FROM users " +
            "WHERE username = #{username} " +
            "  AND password = #{password} " +
            "  AND role = #{role}")
    User findByUsernameAndPasswordAndRole(@Param("username") String username,
                                          @Param("password") String password,
                                          @Param("role")     String role);
}
