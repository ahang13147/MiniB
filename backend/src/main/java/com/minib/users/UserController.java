package com.minib.users;

import com.minib.auth.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.minib.users.UserDtos.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Map<String, User> USERS = new ConcurrentHashMap<>();

    static {
        // Seed demo users
        seedUser("admin", "超级管理员", "admin@minib.edu", "13800138000", Role.SUPER_ADMIN, "系统部", "系统管理员");
        seedUser("uadmin", "高校管理员", "uadmin@minib.edu", "13800138001", Role.UNIVERSITY_ADMIN, "教务处", "教务主任");
        seedUser("eadmin", "企业管理员", "eadmin@company.com", "13800138002", Role.ENTERPRISE_ADMIN, "人事部", "人事经理");
        seedUser("teacher", "张教授", "teacher@minib.edu", "13800138003", Role.TEACHER, "计算机学院", "副教授");
        seedUser("student", "李同学", "student@minib.edu", "13800138004", Role.STUDENT, "计算机学院", "研究生");
        seedUser("mentor", "王导师", "mentor@company.com", "13800138005", Role.ENTERPRISE_MENTOR, "技术部", "高级工程师");
        
        // Additional demo users
        seedUser("teacher2", "刘老师", "teacher2@minib.edu", "13800138006", Role.TEACHER, "软件学院", "讲师");
        seedUser("student2", "陈同学", "student2@minib.edu", "13800138007", Role.STUDENT, "软件学院", "本科生");
        seedUser("student3", "赵同学", "student3@minib.edu", "13800138008", Role.STUDENT, "计算机学院", "本科生");
    }

    private static void seedUser(String username, String displayName, String email, String phone, Role role, String department, String position) {
        User user = new User();
        user.id = UUID.randomUUID().toString();
        user.username = username;
        user.displayName = displayName;
        user.email = email;
        user.phone = phone;
        user.role = role;
        user.department = department;
        user.position = position;
        user.enabled = true;
        user.createdAt = LocalDateTime.now().minusDays((int)(Math.random() * 365));
        user.lastLoginAt = LocalDateTime.now().minusDays((int)(Math.random() * 30));
        USERS.put(user.id, user);
    }

    @GetMapping
    public ResponseEntity<PageResult<User>> listUsers(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "department", required = false) String department,
            @RequestParam(name = "enabled", required = false) Boolean enabled,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        List<User> all = new ArrayList<>(USERS.values());
        List<User> filtered = all.stream().filter(u ->
                (role == null || u.role == role) &&
                (department == null || department.isEmpty() || Objects.equals(u.department, department)) &&
                (enabled == null || u.enabled == enabled) &&
                (keyword == null || keyword.isEmpty() || 
                 (u.username + " " + u.displayName + " " + Objects.toString(u.email, "")).toLowerCase().contains(keyword.toLowerCase()))
        ).sorted(Comparator.comparing(u -> u.username)).collect(Collectors.toList());
        
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(filtered.size(), from + pageSize);
        PageResult<User> result = new PageResult<>();
        result.total = filtered.size();
        result.items = from < to ? filtered.subList(from, to) : List.of();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        User user = USERS.get(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Validated @RequestBody CreateUserRequest req) {
        // Check if username already exists
        boolean usernameExists = USERS.values().stream().anyMatch(u -> u.username.equals(req.username));
        if (usernameExists) {
            return ResponseEntity.badRequest().build();
        }

        User user = new User();
        user.id = UUID.randomUUID().toString();
        user.username = req.username;
        user.displayName = req.displayName != null ? req.displayName : req.username;
        user.email = req.email;
        user.phone = req.phone;
        user.role = req.role;
        user.department = req.department;
        user.position = req.position;
        user.enabled = true;
        user.createdAt = LocalDateTime.now();
        user.lastLoginAt = null;

        USERS.put(user.id, user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @Validated @RequestBody UpdateUserRequest req) {
        User user = USERS.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (req.displayName != null) user.displayName = req.displayName;
        if (req.email != null) user.email = req.email;
        if (req.phone != null) user.phone = req.phone;
        if (req.role != null) user.role = req.role;
        if (req.department != null) user.department = req.department;
        if (req.position != null) user.position = req.position;
        if (req.enabled != null) user.enabled = req.enabled;

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        User removed = USERS.remove(id);
        return removed != null ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        List<String> departments = USERS.values().stream()
                .map(u -> u.department)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(departments);
    }
}

