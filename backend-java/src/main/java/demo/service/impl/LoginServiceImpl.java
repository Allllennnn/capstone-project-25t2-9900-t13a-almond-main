package demo.service.impl;

import demo.mapper.LoginMapper;
import demo.pojo.User;
import demo.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import org.springframework.util.DigestUtils;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginMapper loginMapper;

    @Override
    public User authenticate(String username, String password, String role) {
        // 1. Hash the plain-text password with MD5 (can switch to BCrypt)
        // String digest = DigestUtils.md5DigestAsHex(password.getBytes());
        // 2. Query the user by username, hashed password, and role
        // User user = loginMapper.findByUsernameAndPasswordAndRole(username, digest, role);

        // Omit DigestUtils; remove related import if desired
        User user = loginMapper.findByUsernameAndPasswordAndRole(username, password, role);

        // 3. Only allow login if status is ACTIVE
        if (user != null && "ACTIVE".equals(user.getStatus())) {
            return user;
        }
        return null;
    }
}
