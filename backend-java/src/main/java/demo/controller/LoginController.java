package demo.controller;

import demo.pojo.LoginRequest;
import demo.pojo.Result;
import demo.pojo.User;
import demo.service.LoginService;
import demo.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    /**
     * General login endpoint: ADMIN / TEACHER / STUDENT
     */
    @PostMapping
    public Result login(@RequestBody LoginRequest req) {
        log.info("Login request: {}", req);
        User user = loginService.authenticate(req.getUsername(), req.getPassword(), req.getRole());
        if (user == null) {
            return Result.error("Invalid username or password, or account not activated");
        }
        // Generate JWT with userId and role in payload
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        String token = JwtUtils.generateJwt(claims);

        // Return response containing token and userId
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("user", user);

        return Result.success(response);
    }
}
