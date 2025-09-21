package com.minib.auth;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.minib.auth.AuthDtos.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtService jwtService;

    public AuthController() {
        // DEMO: 简化使用固定密钥与 8 小时过期，生产请用配置
        this.jwtService = new JwtService("01234567890123456789012345678901", 8 * 60 * 60 * 1000);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Validated @RequestBody LoginRequest req) {
        // DEMO: 简化身份校验，生产请替换为数据库校验
        Role role = switch (req.username) {
            case "admin" -> Role.SUPER_ADMIN;
            case "uadmin" -> Role.UNIVERSITY_ADMIN;
            case "eadmin" -> Role.ENTERPRISE_ADMIN;
            case "teacher" -> Role.TEACHER;
            case "student" -> Role.STUDENT;
            case "mentor" -> Role.ENTERPRISE_MENTOR;
            default -> Role.STUDENT;
        };
        List<Permission> permissions = defaultPermissions(role);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());
        claims.put("perms", permissions);

        String token = jwtService.generate(claims, req.username);

        UserProfile profile = new UserProfile();
        profile.id = UUID.randomUUID().toString();
        profile.displayName = capitalize(req.username);
        profile.role = role;
        profile.permissions = permissions;

        LoginResponse resp = new LoginResponse();
        resp.token = token;
        resp.user = profile;
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String token = authorization.replace("Bearer ", "");
        Claims claims = jwtService.parse(token);
        UserProfile profile = new UserProfile();
        profile.id = claims.getSubject();
        profile.displayName = claims.getSubject();
        profile.role = Role.valueOf((String) claims.get("role"));
        @SuppressWarnings("unchecked")
        List<String> permNames = (List<String>) claims.get("perms");
        profile.permissions = new ArrayList<>();
        for (String p : permNames) {
            profile.permissions.add(Permission.valueOf(p));
        }
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Validated @RequestBody RegisterRequest req) {
        // DEMO: 直接回显并签发 token（实际应保存数据库并校验唯一性）
        Role role = req.role != null ? req.role : Role.STUDENT;
        List<Permission> permissions = defaultPermissions(role);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());
        claims.put("perms", permissions);
        String token = jwtService.generate(claims, req.username);

        UserProfile profile = new UserProfile();
        profile.id = UUID.randomUUID().toString();
        profile.displayName = Optional.ofNullable(req.displayName).orElse(capitalize(req.username));
        profile.role = role;
        profile.permissions = permissions;

        LoginResponse resp = new LoginResponse();
        resp.token = token;
        resp.user = profile;
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/forgot")
    public ResponseEntity<Map<String, String>> forgot(@Validated @RequestBody ForgotRequest req) {
        // DEMO: 返回重置 token（实际应发送邮件/短信）
        String resetToken = Base64.getEncoder().encodeToString((req.username + ":RESET").getBytes());
        return ResponseEntity.ok(Map.of("resetToken", resetToken));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> reset(@Validated @RequestBody ResetRequest req) {
        // DEMO: 校验重置 token（演示中总是通过）
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private static List<Permission> defaultPermissions(Role role) {
        return switch (role) {
            case SUPER_ADMIN -> List.of(
                // 基础权限
                Permission.DASHBOARD_VIEW, Permission.PROFILE_MANAGE,
                // 用户管理
                Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_UPDATE, Permission.USER_DELETE, Permission.USER_MANAGE,
                // 企业管理
                Permission.COMPANY_VIEW, Permission.COMPANY_CREATE, Permission.COMPANY_UPDATE, Permission.COMPANY_DELETE, Permission.COMPANY_MANAGE, Permission.COMPANY_APPROVE,
                // 课程管理
                Permission.COURSE_VIEW, Permission.COURSE_CREATE, Permission.COURSE_UPDATE, Permission.COURSE_DELETE, Permission.COURSE_MANAGE,
                // 学生管理
                Permission.STUDENT_VIEW, Permission.STUDENT_CREATE, Permission.STUDENT_UPDATE, Permission.STUDENT_DELETE, Permission.STUDENT_MANAGE,
                // 教师管理
                Permission.TEACHER_VIEW, Permission.TEACHER_CREATE, Permission.TEACHER_UPDATE, Permission.TEACHER_DELETE, Permission.TEACHER_MANAGE,
                // 项目管理
                Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE, Permission.PROJECT_DELETE, Permission.PROJECT_MANAGE, Permission.PROJECT_APPROVE,
                // 实习管理
                Permission.INTERNSHIP_VIEW, Permission.INTERNSHIP_CREATE, Permission.INTERNSHIP_UPDATE, Permission.INTERNSHIP_DELETE, Permission.INTERNSHIP_MANAGE,
                // 资源管理
                Permission.RESOURCE_VIEW, Permission.RESOURCE_CREATE, Permission.RESOURCE_UPDATE, Permission.RESOURCE_DELETE, Permission.RESOURCE_MANAGE,
                // 成果管理
                Permission.ACHIEVEMENT_VIEW, Permission.ACHIEVEMENT_CREATE, Permission.ACHIEVEMENT_UPDATE, Permission.ACHIEVEMENT_DELETE, Permission.ACHIEVEMENT_MANAGE, Permission.ACHIEVEMENT_VERIFY,
                // 导师管理
                Permission.MENTOR_VIEW, Permission.MENTOR_CREATE, Permission.MENTOR_UPDATE, Permission.MENTOR_DELETE, Permission.MENTOR_MANAGE
            );
            
            case UNIVERSITY_ADMIN -> List.of(
                // 基础权限
                Permission.DASHBOARD_VIEW, Permission.PROFILE_MANAGE,
                // 用户管理（限制）
                Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_UPDATE,
                // 课程管理
                Permission.COURSE_VIEW, Permission.COURSE_CREATE, Permission.COURSE_UPDATE, Permission.COURSE_DELETE, Permission.COURSE_MANAGE,
                // 学生管理
                Permission.STUDENT_VIEW, Permission.STUDENT_CREATE, Permission.STUDENT_UPDATE, Permission.STUDENT_DELETE, Permission.STUDENT_MANAGE,
                // 教师管理
                Permission.TEACHER_VIEW, Permission.TEACHER_CREATE, Permission.TEACHER_UPDATE, Permission.TEACHER_MANAGE,
                // 项目管理
                Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE, Permission.PROJECT_MANAGE,
                // 实习管理
                Permission.INTERNSHIP_VIEW,
                // 资源管理
                Permission.RESOURCE_VIEW, Permission.RESOURCE_CREATE, Permission.RESOURCE_UPDATE, Permission.RESOURCE_MANAGE,
                // 成果管理
                Permission.ACHIEVEMENT_VIEW, Permission.ACHIEVEMENT_MANAGE, Permission.ACHIEVEMENT_VERIFY,
                // 导师管理
                Permission.MENTOR_VIEW, Permission.MENTOR_CREATE, Permission.MENTOR_UPDATE, Permission.MENTOR_MANAGE
            );
            
            case ENTERPRISE_ADMIN -> List.of(
                // 基础权限
                Permission.DASHBOARD_VIEW, Permission.PROFILE_MANAGE,
                // 企业管理（自己的企业）
                Permission.COMPANY_VIEW, Permission.COMPANY_UPDATE,
                // 项目管理
                Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE, Permission.PROJECT_MANAGE,
                // 实习管理
                Permission.INTERNSHIP_VIEW, Permission.INTERNSHIP_CREATE, Permission.INTERNSHIP_UPDATE, Permission.INTERNSHIP_DELETE, Permission.INTERNSHIP_MANAGE,
                // 资源管理
                Permission.RESOURCE_VIEW, Permission.RESOURCE_CREATE, Permission.RESOURCE_UPDATE, Permission.RESOURCE_MANAGE,
                // 成果管理
                Permission.ACHIEVEMENT_VIEW,
                // 导师管理
                Permission.MENTOR_VIEW, Permission.MENTOR_CREATE, Permission.MENTOR_UPDATE, Permission.MENTOR_MANAGE,
                // 学生查看
                Permission.STUDENT_VIEW
            );
            
            case TEACHER -> List.of(
                // 基础权限
                Permission.DASHBOARD_VIEW, Permission.PROFILE_MANAGE,
                // 课程管理
                Permission.COURSE_VIEW, Permission.COURSE_CREATE, Permission.COURSE_UPDATE, Permission.COURSE_MANAGE,
                // 学生管理
                Permission.STUDENT_VIEW, Permission.STUDENT_UPDATE,
                // 项目管理
                Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE,
                // 实习管理
                Permission.INTERNSHIP_VIEW,
                // 资源管理
                Permission.RESOURCE_VIEW, Permission.RESOURCE_CREATE, Permission.RESOURCE_UPDATE, Permission.RESOURCE_BORROW,
                // 成果管理
                Permission.ACHIEVEMENT_VIEW, Permission.ACHIEVEMENT_VERIFY,
                // 导师管理
                Permission.MENTOR_VIEW, Permission.MENTOR_CREATE, Permission.MENTOR_UPDATE
            );
            
            case STUDENT -> List.of(
                // 基础权限
                Permission.DASHBOARD_VIEW, Permission.PROFILE_MANAGE,
                // 项目查看
                Permission.PROJECT_VIEW,
                // 实习管理
                Permission.INTERNSHIP_VIEW, Permission.INTERNSHIP_APPLY,
                // 资源管理
                Permission.RESOURCE_VIEW, Permission.RESOURCE_BORROW,
                // 成果管理
                Permission.ACHIEVEMENT_VIEW, Permission.ACHIEVEMENT_CREATE, Permission.ACHIEVEMENT_UPDATE, Permission.ACHIEVEMENT_DELETE,
                // 导师查看
                Permission.MENTOR_VIEW
            );
            
            case ENTERPRISE_MENTOR -> List.of(
                // 基础权限
                Permission.DASHBOARD_VIEW, Permission.PROFILE_MANAGE,
                // 学生查看
                Permission.STUDENT_VIEW,
                // 项目管理
                Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE,
                // 实习管理
                Permission.INTERNSHIP_VIEW, Permission.INTERNSHIP_CREATE, Permission.INTERNSHIP_UPDATE,
                // 资源管理
                Permission.RESOURCE_VIEW, Permission.RESOURCE_CREATE, Permission.RESOURCE_UPDATE, Permission.RESOURCE_BORROW,
                // 成果管理
                Permission.ACHIEVEMENT_VIEW,
                // 导师管理
                Permission.MENTOR_VIEW, Permission.MENTOR_UPDATE
            );
        };
    }
}




