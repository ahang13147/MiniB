package com.minib.students;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.minib.students.StudentDtos.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private static final Map<String, Student> STUDENTS = new ConcurrentHashMap<>();
    private static final Map<String, List<Grade>> STUDENT_GRADES = new ConcurrentHashMap<>();

    static {
        // Seed demo students
        seedStudent("student", "李同学", "student@minib.edu", "13800138004", "2021001", "计算机学院", "计算机科学与技术", "2021级", "1班");
        seedStudent("student2", "陈同学", "student2@minib.edu", "13800138007", "2021002", "软件学院", "软件工程", "2021级", "2班");
        seedStudent("student3", "赵同学", "student3@minib.edu", "13800138008", "2021003", "计算机学院", "计算机科学与技术", "2022级", "1班");
        seedStudent("student4", "王同学", "student4@minib.edu", "13800138009", "2022001", "软件学院", "软件工程", "2022级", "1班");
        seedStudent("student5", "刘同学", "student5@minib.edu", "13800138010", "2022002", "计算机学院", "人工智能", "2022级", "2班");
        seedStudent("student6", "张同学", "student6@minib.edu", "13800138011", "2023001", "软件学院", "数据科学与大数据技术", "2023级", "1班");
    }

    private static void seedStudent(String username, String displayName, String email, String phone, 
                                   String studentId, String department, String major, String grade, String class_) {
        Student student = new Student();
        student.id = UUID.randomUUID().toString();
        student.username = username;
        student.displayName = displayName;
        student.email = email;
        student.phone = phone;
        student.studentId = studentId;
        student.department = department;
        student.major = major;
        student.grade = grade;
        student.class_ = class_;
        student.status = "ENROLLED";
        student.enrollmentDate = LocalDateTime.now().minusYears(2).plusDays((int)(Math.random() * 365));
        student.createdAt = LocalDateTime.now().minusDays((int)(Math.random() * 365));
        student.lastLoginAt = LocalDateTime.now().minusDays((int)(Math.random() * 30));
        
        // Random courses
        List<String> courses = new ArrayList<>();
        if (Math.random() > 0.2) courses.add("CS101");
        if (Math.random() > 0.3) courses.add("CS102");
        if (Math.random() > 0.4) courses.add("CS201");
        student.courses = courses;
        student.courseCount = courses.size();
        
        // Random projects
        List<String> projects = new ArrayList<>();
        if (Math.random() > 0.5) projects.add("毕业设计-" + displayName);
        if (Math.random() > 0.7) projects.add("实习项目-" + displayName);
        student.projects = projects;
        student.projectCount = projects.size();
        
        // Generate random grades
        List<Grade> grades = generateRandomGrades(studentId);
        student.grades = grades;
        student.gpa = calculateGPA(grades);
        
        STUDENTS.put(student.id, student);
        STUDENT_GRADES.put(student.id, new ArrayList<>(grades));
    }

    private static List<Grade> generateRandomGrades(String studentId) {
        List<Grade> grades = new ArrayList<>();
        String[] courses = {"CS101", "CS102", "CS201", "CS301", "CS302"};
        String[] courseNames = {"数据结构与算法", "操作系统", "软件工程", "人工智能", "机器学习"};
        int[] credits = {3, 3, 4, 3, 4};
        String[] semesters = {"2021秋", "2022春", "2022秋", "2023春", "2023秋"};
        
        for (int i = 0; i < courses.length; i++) {
            if (Math.random() > 0.3) {
                Grade grade = new Grade();
                grade.courseId = UUID.randomUUID().toString();
                grade.courseName = courseNames[i];
                grade.courseCode = courses[i];
                grade.credits = credits[i];
                grade.semester = semesters[i % semesters.length];
                grade.examDate = LocalDateTime.now().minusDays((int)(Math.random() * 180));
                
                // Random score and grade
                int score = 60 + (int)(Math.random() * 40);
                grade.score = String.valueOf(score);
                grade.grade = scoreToGrade(score);
                
                grades.add(grade);
            }
        }
        return grades;
    }

    private static String scoreToGrade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    private static double calculateGPA(List<Grade> grades) {
        if (grades.isEmpty()) return 0.0;
        
        double totalPoints = 0.0;
        int totalCredits = 0;
        
        for (Grade grade : grades) {
            double points = gradeToPoints(grade.grade);
            totalPoints += points * grade.credits;
            totalCredits += grade.credits;
        }
        
        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }

    private static double gradeToPoints(String grade) {
        return switch (grade) {
            case "A" -> 4.0;
            case "B" -> 3.0;
            case "C" -> 2.0;
            case "D" -> 1.0;
            default -> 0.0;
        };
    }

    @GetMapping
    public ResponseEntity<PageResult<Student>> listStudents(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "department", required = false) String department,
            @RequestParam(name = "major", required = false) String major,
            @RequestParam(name = "grade", required = false) String grade,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        List<Student> all = new ArrayList<>(STUDENTS.values());
        List<Student> filtered = all.stream().filter(s ->
                (department == null || department.isEmpty() || Objects.equals(s.department, department)) &&
                (major == null || major.isEmpty() || Objects.equals(s.major, major)) &&
                (grade == null || grade.isEmpty() || Objects.equals(s.grade, grade)) &&
                (status == null || status.isEmpty() || Objects.equals(s.status, status)) &&
                (keyword == null || keyword.isEmpty() || 
                 (s.username + " " + s.displayName + " " + s.studentId + " " + Objects.toString(s.email, "")).toLowerCase().contains(keyword.toLowerCase()))
        ).sorted(Comparator.comparing(s -> s.studentId)).collect(Collectors.toList());
        
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(filtered.size(), from + pageSize);
        PageResult<Student> result = new PageResult<>();
        result.total = filtered.size();
        result.items = from < to ? filtered.subList(from, to) : List.of();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable String id) {
        Student student = STUDENTS.get(id);
        return student != null ? ResponseEntity.ok(student) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/grades")
    public ResponseEntity<List<Grade>> getStudentGrades(@PathVariable String id) {
        List<Grade> grades = STUDENT_GRADES.get(id);
        return grades != null ? ResponseEntity.ok(grades) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/grades")
    public ResponseEntity<Void> addGrade(@PathVariable String id, @Validated @RequestBody AddGradeRequest req) {
        Student student = STUDENTS.get(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        Grade grade = new Grade();
        grade.courseId = req.courseId;
        grade.courseName = req.courseName;
        grade.courseCode = req.courseCode;
        grade.credits = req.credits;
        grade.score = req.score;
        grade.grade = req.grade;
        grade.semester = req.semester;
        grade.examDate = LocalDateTime.now();

        List<Grade> grades = STUDENT_GRADES.getOrDefault(id, new ArrayList<>());
        grades.add(grade);
        student.grades = new ArrayList<>(grades);
        student.gpa = calculateGPA(grades);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/grades/{gradeId}")
    public ResponseEntity<Void> updateGrade(@PathVariable String id, @PathVariable String gradeId, 
                                           @Validated @RequestBody UpdateGradeRequest req) {
        List<Grade> grades = STUDENT_GRADES.get(id);
        if (grades == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<Grade> gradeOpt = grades.stream()
                .filter(g -> g.courseId.equals(gradeId))
                .findFirst();

        if (gradeOpt.isPresent()) {
            Grade grade = gradeOpt.get();
            if (req.score != null) {
                grade.score = req.score;
                grade.grade = scoreToGrade(Integer.parseInt(req.score));
            }
            if (req.grade != null) {
                grade.grade = req.grade;
            }

            Student student = STUDENTS.get(id);
            if (student != null) {
                student.gpa = calculateGPA(grades);
            }
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/grades/{gradeId}")
    public ResponseEntity<Void> deleteGrade(@PathVariable String id, @PathVariable String gradeId) {
        List<Grade> grades = STUDENT_GRADES.get(id);
        if (grades == null) {
            return ResponseEntity.notFound().build();
        }

        grades.removeIf(g -> g.courseId.equals(gradeId));
        Student student = STUDENTS.get(id);
        if (student != null) {
            student.grades = new ArrayList<>(grades);
            student.gpa = calculateGPA(grades);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        List<String> departments = STUDENTS.values().stream()
                .map(s -> s.department)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/majors")
    public ResponseEntity<List<String>> getMajors() {
        List<String> majors = STUDENTS.values().stream()
                .map(s -> s.major)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(majors);
    }

    @GetMapping("/grades")
    public ResponseEntity<List<String>> getGrades() {
        List<String> grades = STUDENTS.values().stream()
                .map(s -> s.grade)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(grades);
    }
}

