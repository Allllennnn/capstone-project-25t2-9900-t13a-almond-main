package demo.interceptor;

import com.alibaba.fastjson.JSONObject;
import demo.pojo.Result;
import demo.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String url    = request.getRequestURI();
        String method = request.getMethod();
        log.info("Request URL: {}, method: {}", url, method);

        // 1. Allow login/register
        if (url.startsWith("/login") || url.startsWith("/register")) {
            return true;
        }

        // 2. Retrieve and validate JWT
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasLength(auth) || !auth.startsWith("Bearer ")) {
            return reject(response, 401, "NOT_LOGIN");
        }
        Claims claims;
        try {
            claims = JwtUtils.parseJWT(auth.substring(7));
        } catch (Exception e) {
            return reject(response, 401, "NOT_LOGIN");
        }

        // Inject userId into subsequent controllers
        Long userId = claims.get("userId", Long.class);
        request.setAttribute("userId", userId);

        String role = claims.get("role", String.class);
        log.info("JWT parsed successfully, userId = {}, role = {}", userId, role);

        // 3. Role-based access control
        switch (role) {
            case "ADMIN":
                return true;

            case "TEACHER":
                // Teacher: own management (/teacher), tasks (/task), groups (/group),
                // weekly goals (/member-weekly-goal), assignments (/task-assignment),
                // student (/student), student/group management (/admin/students, /admin/groups),
                // file upload (/upload), file preview (/file)
                if (url.startsWith("/teacher")
                        || url.startsWith("/task")
                        || url.startsWith("/group")
                        || url.startsWith("/member-weekly-goal")
                        || url.startsWith("/task-assignment")
                        || url.startsWith("/student")
                        || url.startsWith("/admin/students")
                        || url.startsWith("/admin/groups")
                        || url.startsWith("/upload")
                        || url.startsWith("/file")) {
                    return true;
                }
                return reject(response, 403, "FORBIDDEN");

            case "STUDENT":
                // Student: allow all endpoints starting with /student
                if (url.startsWith("/student")) {
                    return true;
                }
                // Allow viewing all groups (/groups)
                if ("GET".equals(method) && url.equals("/groups")) {
                    return true;
                }
                // Task assignment related (/task-assignment)
                if (url.startsWith("/task-assignment")) {
                    return true;
                }
                // Meeting management related (/meeting and /student/meetings)
                if (url.startsWith("/meeting") || url.startsWith("/student/meetings")) {
                    return true;
                }
                // File upload and preview permissions (/upload and /file)
                if (url.startsWith("/upload") || url.startsWith("/file")) {
                    return true;
                }
                // Agent analysis endpoint permissions (/agent)
                if (url.startsWith("/agent")) {
                    return true;
                }
                return reject(response, 403, "FORBIDDEN");

            default:
                return reject(response, 403, "FORBIDDEN");
        }
    }

    private boolean reject(HttpServletResponse resp, int status, String code) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(JSONObject.toJSONString(Result.error(code)));
        return false;
    }
}
