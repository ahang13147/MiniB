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
            case SUPER_ADMIN -> List.of(Permission.USER_MANAGE, Permission.COURSE_MANAGE, Permission.COMPANY_MANAGE, Permission.DASHBOARD_VIEW);
            case UNIVERSITY_ADMIN -> List.of(Permission.USER_MANAGE, Permission.COURSE_MANAGE, Permission.DASHBOARD_VIEW, Permission.TEACHER_VIEW, Permission.STUDENT_VIEW);
            case ENTERPRISE_ADMIN -> List.of(Permission.COMPANY_MANAGE, Permission.DASHBOARD_VIEW, Permission.STUDENT_VIEW);
            case TEACHER -> List.of(Permission.COURSE_MANAGE, Permission.STUDENT_VIEW, Permission.DASHBOARD_VIEW);
            case STUDENT -> List.of(Permission.DASHBOARD_VIEW);
            case ENTERPRISE_MENTOR -> List.of(Permission.STUDENT_VIEW, Permission.DASHBOARD_VIEW);
        };
    }
}




