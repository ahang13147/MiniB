package com.minib.courses;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.minib.courses.CourseDtos.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private static final Map<String, Course> COURSES = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> COURSE_STUDENTS = new ConcurrentHashMap<>();

    static {
        // Seed demo courses
        seedCourse("CS101", "数据结构与算法", "计算机基础课程", "teacher", "张教授", "计算机学院", "2024春", 3, 48);
        seedCourse("CS102", "操作系统", "系统软件基础", "teacher", "张教授", "计算机学院", "2024春", 3, 48);
        seedCourse("CS201", "软件工程", "软件开发方法论", "teacher2", "刘老师", "软件学院", "2024春", 4, 64);
        seedCourse("CS301", "人工智能", "AI基础与应用", "teacher", "张教授", "计算机学院", "2024春", 3, 48);
        seedCourse("CS302", "机器学习", "ML理论与实践", "teacher2", "刘老师", "软件学院", "2024春", 4, 64);
        seedCourse("CS401", "深度学习", "神经网络与深度学习", "teacher", "张教授", "计算机学院", "2024秋", 3, 48);
        seedCourse("CS402", "计算机视觉", "图像处理与识别", "teacher2", "刘老师", "软件学院", "2024秋", 3, 48);
        seedCourse("CS501", "分布式系统", "分布式计算架构", "teacher", "张教授", "计算机学院", "2024秋", 4, 64);
        seedCourse("CS502", "云计算", "云平台与服务", "teacher2", "刘老师", "软件学院", "2024秋", 3, 48);
        seedCourse("CS601", "区块链技术", "分布式账本技术", "teacher", "张教授", "计算机学院", "2024秋", 3, 48);
    }

    private static void seedCourse(String code, String name, String description, String teacherId, String teacherName, 
                                  String department, String semester, int credits, int hours) {
        Course course = new Course();
        course.id = UUID.randomUUID().toString();
        course.code = code;
        course.name = name;
        course.description = description;
        course.teacherId = teacherId;
        course.teacherName = teacherName;
        course.department = department;
        course.semester = semester;
        course.credits = credits;
        course.hours = hours;
        course.status = "PUBLISHED";
        course.createdAt = LocalDateTime.now().minusDays((int)(Math.random() * 100));
        course.updatedAt = LocalDateTime.now().minusDays((int)(Math.random() * 10));
        
        // Random students
        List<String> students = new ArrayList<>();
        if (Math.random() > 0.3) students.add("student");
        if (Math.random() > 0.5) students.add("student2");
        if (Math.random() > 0.7) students.add("student3");
        course.students = students;
        course.studentCount = students.size();
        
        COURSES.put(course.id, course);
        COURSE_STUDENTS.put(course.id, new ArrayList<>(students));
    }

    @GetMapping
    public ResponseEntity<PageResult<Course>> listCourses(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "teacherId", required = false) String teacherId,
            @RequestParam(name = "department", required = false) String department,
            @RequestParam(name = "semester", required = false) String semester,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        List<Course> all = new ArrayList<>(COURSES.values());
        List<Course> filtered = all.stream().filter(c ->
                (teacherId == null || teacherId.isEmpty() || Objects.equals(c.teacherId, teacherId)) &&
                (department == null || department.isEmpty() || Objects.equals(c.department, department)) &&
                (semester == null || semester.isEmpty() || Objects.equals(c.semester, semester)) &&
                (status == null || status.isEmpty() || Objects.equals(c.status, status)) &&
                (keyword == null || keyword.isEmpty() || 
                 (c.code + " " + c.name + " " + Objects.toString(c.description, "")).toLowerCase().contains(keyword.toLowerCase()))
        ).sorted(Comparator.comparing(c -> c.code)).collect(Collectors.toList());
        
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(filtered.size(), from + pageSize);
        PageResult<Course> result = new PageResult<>();
        result.total = filtered.size();
        result.items = from < to ? filtered.subList(from, to) : List.of();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable String id) {
        Course course = COURSES.get(id);
        return course != null ? ResponseEntity.ok(course) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@Validated @RequestBody CreateCourseRequest req) {
        // Check if course code already exists
        boolean codeExists = COURSES.values().stream().anyMatch(c -> c.code.equals(req.code));
        if (codeExists) {
            return ResponseEntity.badRequest().build();
        }

        Course course = new Course();
        course.id = UUID.randomUUID().toString();
        course.code = req.code;
        course.name = req.name;
        course.description = req.description;
        course.teacherId = req.teacherId;
        course.teacherName = getTeacherName(req.teacherId);
        course.department = req.department;
        course.semester = req.semester;
        course.credits = req.credits;
        course.hours = req.hours;
        course.status = "DRAFT";
        course.createdAt = LocalDateTime.now();
        course.updatedAt = LocalDateTime.now();
        course.students = new ArrayList<>();
        course.studentCount = 0;

        COURSES.put(course.id, course);
        COURSE_STUDENTS.put(course.id, new ArrayList<>());
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable String id, @Validated @RequestBody UpdateCourseRequest req) {
        Course course = COURSES.get(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        if (req.name != null) course.name = req.name;
        if (req.description != null) course.description = req.description;
        if (req.teacherId != null) {
            course.teacherId = req.teacherId;
            course.teacherName = getTeacherName(req.teacherId);
        }
        if (req.department != null) course.department = req.department;
        if (req.semester != null) course.semester = req.semester;
        if (req.credits != null) course.credits = req.credits;
        if (req.hours != null) course.hours = req.hours;
        if (req.status != null) course.status = req.status;

        course.updatedAt = LocalDateTime.now();

        return ResponseEntity.ok(course);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
        Course removed = COURSES.remove(id);
        COURSE_STUDENTS.remove(id);
        return removed != null ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/students")
    public ResponseEntity<Void> assignStudent(@PathVariable String id, @Validated @RequestBody AssignStudentRequest req) {
        Course course = COURSES.get(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> students = COURSE_STUDENTS.getOrDefault(id, new ArrayList<>());
        if (!students.contains(req.studentId)) {
            students.add(req.studentId);
            course.students = new ArrayList<>(students);
            course.studentCount = students.size();
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/students")
    public ResponseEntity<Void> removeStudent(@PathVariable String id, @Validated @RequestBody RemoveStudentRequest req) {
        Course course = COURSES.get(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> students = COURSE_STUDENTS.getOrDefault(id, new ArrayList<>());
        students.remove(req.studentId);
        course.students = new ArrayList<>(students);
        course.studentCount = students.size();

        return ResponseEntity.ok().build();
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<Map<String, String>>> getTeachers() {
        List<Map<String, String>> teachers = List.of(
            Map.of("id", "teacher", "name", "张教授"),
            Map.of("id", "teacher2", "name", "刘老师")
        );
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        List<String> departments = COURSES.values().stream()
                .map(c -> c.department)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/semesters")
    public ResponseEntity<List<String>> getSemesters() {
        List<String> semesters = COURSES.values().stream()
                .map(c -> c.semester)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(semesters);
    }

    private String getTeacherName(String teacherId) {
        return switch (teacherId) {
            case "teacher" -> "张教授";
            case "teacher2" -> "刘老师";
            default -> "未知教师";
        };
    }
}

