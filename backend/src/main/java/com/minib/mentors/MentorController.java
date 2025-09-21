package com.minib.mentors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.minib.mentors.MentorDtos.*;

@RestController
@RequestMapping("/api/mentors")
@CrossOrigin(origins = "*")
public class MentorController {
    
    // 模拟数据存储
    private static final Map<String, DualMentorCourse> COURSES = new ConcurrentHashMap<>();
    private static final Map<String, CourseEnrollment> ENROLLMENTS = new ConcurrentHashMap<>();
    private static final Map<String, EnterpriseMentor> MENTORS = new ConcurrentHashMap<>();
    
    static {
        // 初始化示例数据
        initSampleData();
    }
    
    // ================================
    // 双导师课堂管理
    // ================================
    
    @GetMapping("/courses")
    public ResponseEntity<PageResult<DualMentorCourse>> getCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String universityId,
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) TeachingMode mode) {
        
        List<DualMentorCourse> filtered = COURSES.values().stream()
                .filter(c -> search == null || c.courseName.contains(search) || c.description.contains(search))
                .filter(c -> universityId == null || c.universityId.equals(universityId))
                .filter(c -> companyId == null || c.companyId.equals(companyId))
                .filter(c -> status == null || c.status == status)
                .filter(c -> mode == null || c.teachingMode == mode)
                .sorted((a, b) -> b.createdAt.compareTo(a.createdAt))
                .collect(Collectors.toList());
        
        int total = filtered.size();
        List<DualMentorCourse> items = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        
        PageResult<DualMentorCourse> result = new PageResult<>();
        result.items = items;
        result.total = total;
        result.page = page;
        result.size = size;
        result.totalPages = (total + size - 1) / size;
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/courses/{id}")
    public ResponseEntity<DualMentorCourse> getCourse(@PathVariable String id) {
        DualMentorCourse course = COURSES.get(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 加载选课信息
        course.enrollments = ENROLLMENTS.values().stream()
                .filter(e -> e.courseId.equals(id))
                .sorted((a, b) -> a.enrollmentDate.compareTo(b.enrollmentDate))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(course);
    }
    
    @PostMapping("/courses")
    public ResponseEntity<DualMentorCourse> createCourse(@Validated @RequestBody CreateDualCourseRequest request) {
        DualMentorCourse course = new DualMentorCourse();
        course.id = UUID.randomUUID().toString();
        course.courseCode = request.courseCode;
        course.courseName = request.courseName;
        course.description = request.description;
        course.universityId = request.universityId;
        course.universityName = getUniversityName(request.universityId);
        course.companyId = request.companyId;
        course.companyName = getCompanyName(request.companyId);
        course.universityTeacherId = request.universityTeacherId;
        course.universityTeacherName = getUserName(request.universityTeacherId);
        course.enterpriseMentorId = request.enterpriseMentorId;
        course.enterpriseMentorName = getUserName(request.enterpriseMentorId);
        course.department = request.department;
        course.majorId = request.majorId;
        course.majorName = request.majorId != null ? getMajorName(request.majorId) : null;
        course.semester = request.semester;
        course.academicYear = request.academicYear;
        course.credits = request.credits;
        course.totalHours = request.totalHours;
        course.theoryHours = request.theoryHours;
        course.practiceHours = request.practiceHours;
        course.maxStudents = request.maxStudents;
        course.enrolledStudents = 0;
        course.courseType = request.courseType;
        course.teachingMode = request.teachingMode;
        course.status = CourseStatus.PLANNING;
        course.startDate = request.startDate;
        course.endDate = request.endDate;
        course.createdAt = LocalDateTime.now();
        course.updatedAt = LocalDateTime.now();
        course.enrollments = new ArrayList<>();
        
        COURSES.put(course.id, course);
        return ResponseEntity.ok(course);
    }
    
    @PutMapping("/courses/{id}")
    public ResponseEntity<DualMentorCourse> updateCourse(@PathVariable String id,
                                                        @Validated @RequestBody UpdateDualCourseRequest request) {
        DualMentorCourse course = COURSES.get(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.courseName != null) course.courseName = request.courseName;
        if (request.description != null) course.description = request.description;
        if (request.universityTeacherId != null) {
            course.universityTeacherId = request.universityTeacherId;
            course.universityTeacherName = getUserName(request.universityTeacherId);
        }
        if (request.enterpriseMentorId != null) {
            course.enterpriseMentorId = request.enterpriseMentorId;
            course.enterpriseMentorName = getUserName(request.enterpriseMentorId);
        }
        if (request.department != null) course.department = request.department;
        if (request.majorId != null) {
            course.majorId = request.majorId;
            course.majorName = getMajorName(request.majorId);
        }
        if (request.semester != null) course.semester = request.semester;
        if (request.academicYear != null) course.academicYear = request.academicYear;
        if (request.credits != null) course.credits = request.credits;
        if (request.totalHours != null) course.totalHours = request.totalHours;
        if (request.theoryHours != null) course.theoryHours = request.theoryHours;
        if (request.practiceHours != null) course.practiceHours = request.practiceHours;
        if (request.maxStudents != null) course.maxStudents = request.maxStudents;
        if (request.courseType != null) course.courseType = request.courseType;
        if (request.teachingMode != null) course.teachingMode = request.teachingMode;
        if (request.status != null) course.status = request.status;
        if (request.startDate != null) course.startDate = request.startDate;
        if (request.endDate != null) course.endDate = request.endDate;
        
        course.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(course);
    }
    
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
        DualMentorCourse course = COURSES.remove(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 删除相关选课记录
        ENROLLMENTS.values().removeIf(e -> e.courseId.equals(id));
        
        return ResponseEntity.noContent().build();
    }
    
    // ================================
    // 选课管理
    // ================================
    
    @GetMapping("/enrollments")
    public ResponseEntity<PageResult<CourseEnrollment>> getEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) EnrollmentStatus status) {
        
        List<CourseEnrollment> filtered = ENROLLMENTS.values().stream()
                .filter(e -> courseId == null || e.courseId.equals(courseId))
                .filter(e -> studentId == null || e.studentId.equals(studentId))
                .filter(e -> status == null || e.status == status)
                .sorted((a, b) -> b.enrollmentDate.compareTo(a.enrollmentDate))
                .collect(Collectors.toList());
        
        int total = filtered.size();
        List<CourseEnrollment> items = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        
        PageResult<CourseEnrollment> result = new PageResult<>();
        result.items = items;
        result.total = total;
        result.page = page;
        result.size = size;
        result.totalPages = (total + size - 1) / size;
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/enroll")
    public ResponseEntity<CourseEnrollment> enrollCourse(@Validated @RequestBody EnrollCourseRequest request) {
        DualMentorCourse course = COURSES.get(request.courseId);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (course.status != CourseStatus.RECRUITING) {
            return ResponseEntity.badRequest().build();
        }
        
        if (course.enrolledStudents >= course.maxStudents) {
            return ResponseEntity.badRequest().build();
        }
        
        // 检查是否已选课
        boolean alreadyEnrolled = ENROLLMENTS.values().stream()
                .anyMatch(e -> e.courseId.equals(request.courseId) && e.studentId.equals(request.studentId));
        if (alreadyEnrolled) {
            return ResponseEntity.badRequest().build();
        }
        
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.id = UUID.randomUUID().toString();
        enrollment.courseId = request.courseId;
        enrollment.courseName = course.courseName;
        enrollment.studentId = request.studentId;
        enrollment.studentName = getStudentName(request.studentId);
        enrollment.studentMajor = getStudentMajor(request.studentId);
        enrollment.enrollmentDate = LocalDate.now();
        enrollment.status = EnrollmentStatus.ENROLLED;
        enrollment.createdAt = LocalDateTime.now();
        enrollment.updatedAt = LocalDateTime.now();
        
        ENROLLMENTS.put(enrollment.id, enrollment);
        
        // 更新课程选课人数
        course.enrolledStudents = course.enrolledStudents + 1;
        
        return ResponseEntity.ok(enrollment);
    }
    
    @PutMapping("/enrollments/{id}")
    public ResponseEntity<CourseEnrollment> updateEnrollment(@PathVariable String id,
                                                           @Validated @RequestBody UpdateEnrollmentRequest request) {
        CourseEnrollment enrollment = ENROLLMENTS.get(id);
        if (enrollment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.status != null) enrollment.status = request.status;
        if (request.finalScore != null) enrollment.finalScore = request.finalScore;
        if (request.finalGrade != null) enrollment.finalGrade = request.finalGrade;
        if (request.theoryScore != null) enrollment.theoryScore = request.theoryScore;
        if (request.practiceScore != null) enrollment.practiceScore = request.practiceScore;
        if (request.attendanceRate != null) enrollment.attendanceRate = request.attendanceRate;
        
        enrollment.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(enrollment);
    }
    
    // ================================
    // 企业导师管理
    // ================================
    
    @GetMapping("/enterprise-mentors")
    public ResponseEntity<PageResult<EnterpriseMentor>> getMentors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) MentorStatus status) {
        
        List<EnterpriseMentor> filtered = MENTORS.values().stream()
                .filter(m -> search == null || m.name.contains(search) || m.expertise.contains(search))
                .filter(m -> companyId == null || m.companyId.equals(companyId))
                .filter(m -> status == null || m.status == status)
                .sorted((a, b) -> b.createdAt.compareTo(a.createdAt))
                .collect(Collectors.toList());
        
        int total = filtered.size();
        List<EnterpriseMentor> items = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        
        // 设置课程和学生数量
        items.forEach(mentor -> {
            mentor.courseCount = (int) COURSES.values().stream()
                    .filter(c -> c.enterpriseMentorId.equals(mentor.userId))
                    .count();
            mentor.studentCount = (int) ENROLLMENTS.values().stream()
                    .filter(e -> {
                        DualMentorCourse course = COURSES.get(e.courseId);
                        return course != null && course.enterpriseMentorId.equals(mentor.userId);
                    })
                    .count();
        });
        
        PageResult<EnterpriseMentor> result = new PageResult<>();
        result.items = items;
        result.total = total;
        result.page = page;
        result.size = size;
        result.totalPages = (total + size - 1) / size;
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/enterprise-mentors/{id}")
    public ResponseEntity<EnterpriseMentor> getMentor(@PathVariable String id) {
        EnterpriseMentor mentor = MENTORS.get(id);
        if (mentor == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 加载课程信息
        mentor.courses = COURSES.values().stream()
                .filter(c -> c.enterpriseMentorId.equals(mentor.userId))
                .map(c -> c.id)
                .collect(Collectors.toList());
        mentor.courseCount = mentor.courses.size();
        
        // 加载学生信息
        mentor.students = ENROLLMENTS.values().stream()
                .filter(e -> {
                    DualMentorCourse course = COURSES.get(e.courseId);
                    return course != null && course.enterpriseMentorId.equals(mentor.userId);
                })
                .map(e -> e.studentId)
                .distinct()
                .collect(Collectors.toList());
        mentor.studentCount = mentor.students.size();
        
        return ResponseEntity.ok(mentor);
    }
    
    @PostMapping("/enterprise-mentors")
    public ResponseEntity<EnterpriseMentor> registerMentor(@Validated @RequestBody RegisterMentorRequest request) {
        EnterpriseMentor mentor = new EnterpriseMentor();
        mentor.id = UUID.randomUUID().toString();
        mentor.userId = request.userId;
        mentor.name = getUserName(request.userId);
        mentor.email = getUserEmail(request.userId);
        mentor.phone = getUserPhone(request.userId);
        mentor.companyId = request.companyId;
        mentor.companyName = getCompanyName(request.companyId);
        mentor.position = request.position;
        mentor.department = request.department;
        mentor.workYears = request.workYears;
        mentor.expertise = request.expertise;
        mentor.education = request.education;
        mentor.certifications = request.certifications;
        mentor.introduction = request.introduction;
        mentor.status = MentorStatus.ACTIVE;
        mentor.createdAt = LocalDateTime.now();
        mentor.updatedAt = LocalDateTime.now();
        mentor.courses = new ArrayList<>();
        mentor.courseCount = 0;
        mentor.students = new ArrayList<>();
        mentor.studentCount = 0;
        
        MENTORS.put(mentor.id, mentor);
        return ResponseEntity.ok(mentor);
    }
    
    @PutMapping("/enterprise-mentors/{id}")
    public ResponseEntity<EnterpriseMentor> updateMentor(@PathVariable String id,
                                                        @Validated @RequestBody UpdateMentorRequest request) {
        EnterpriseMentor mentor = MENTORS.get(id);
        if (mentor == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.position != null) mentor.position = request.position;
        if (request.department != null) mentor.department = request.department;
        if (request.workYears != null) mentor.workYears = request.workYears;
        if (request.expertise != null) mentor.expertise = request.expertise;
        if (request.education != null) mentor.education = request.education;
        if (request.certifications != null) mentor.certifications = request.certifications;
        if (request.introduction != null) mentor.introduction = request.introduction;
        if (request.status != null) mentor.status = request.status;
        
        mentor.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(mentor);
    }
    
    // ================================
    // 统计信息
    // ================================
    
    @GetMapping("/stats")
    public ResponseEntity<MentorStats> getMentorStats() {
        MentorStats stats = new MentorStats();
        
        List<DualMentorCourse> allCourses = new ArrayList<>(COURSES.values());
        List<EnterpriseMentor> allMentors = new ArrayList<>(MENTORS.values());
        List<CourseEnrollment> allEnrollments = new ArrayList<>(ENROLLMENTS.values());
        
        stats.totalCourses = allCourses.size();
        stats.activeCourses = allCourses.stream()
                .filter(c -> c.status == CourseStatus.IN_PROGRESS)
                .count();
        stats.completedCourses = allCourses.stream()
                .filter(c -> c.status == CourseStatus.COMPLETED)
                .count();
        stats.totalMentors = allMentors.size();
        stats.activeMentors = allMentors.stream()
                .filter(m -> m.status == MentorStatus.ACTIVE)
                .count();
        stats.totalEnrollments = allEnrollments.size();
        stats.completedEnrollments = allEnrollments.stream()
                .filter(e -> e.status == EnrollmentStatus.COMPLETED)
                .count();
        
        // 计算平均分数
        OptionalDouble avgScore = allEnrollments.stream()
                .filter(e -> e.finalScore != null)
                .mapToDouble(e -> e.finalScore)
                .average();
        stats.averageScore = avgScore.isPresent() ? avgScore.getAsDouble() : 0.0;
        
        // 企业统计
        stats.topCompanies = allMentors.stream()
                .collect(Collectors.groupingBy(m -> m.companyId, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CompanyMentorStats companyStats = new CompanyMentorStats();
                    companyStats.companyId = entry.getKey();
                    companyStats.companyName = entry.getValue().get(0).companyName;
                    companyStats.mentorCount = entry.getValue().size();
                    companyStats.courseCount = allCourses.stream()
                            .filter(c -> entry.getValue().stream()
                                    .anyMatch(m -> m.userId.equals(c.enterpriseMentorId)))
                            .count();
                    return companyStats;
                })
                .sorted((a, b) -> Long.compare(b.mentorCount, a.mentorCount))
                .limit(10)
                .collect(Collectors.toList());
        
        // 教学模式统计
        stats.modeStats = Arrays.stream(TeachingMode.values())
                .map(mode -> {
                    CourseModeStats modeStats = new CourseModeStats();
                    modeStats.mode = mode;
                    modeStats.count = allCourses.stream()
                            .filter(c -> c.teachingMode == mode)
                            .count();
                    modeStats.enrollmentCount = allEnrollments.stream()
                            .filter(e -> {
                                DualMentorCourse course = COURSES.get(e.courseId);
                                return course != null && course.teachingMode == mode;
                            })
                            .count();
                    return modeStats;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(stats);
    }
    
    // ================================
    // 辅助方法
    // ================================
    
    private String getUniversityName(String universityId) {
        return "示例大学"; // 实际应从数据库查询
    }
    
    private String getCompanyName(String companyId) {
        return "示例企业"; // 实际应从数据库查询
    }
    
    private String getUserName(String userId) {
        return "用户" + userId.substring(0, 8); // 实际应从数据库查询
    }
    
    private String getUserEmail(String userId) {
        return "user" + userId.substring(0, 8) + "@company.com"; // 实际应从数据库查询
    }
    
    private String getUserPhone(String userId) {
        return "138****" + userId.substring(0, 4); // 实际应从数据库查询
    }
    
    private String getMajorName(String majorId) {
        return "计算机科学与技术"; // 实际应从数据库查询
    }
    
    private String getStudentName(String studentId) {
        return "学生" + studentId.substring(0, 8); // 实际应从数据库查询
    }
    
    private String getStudentMajor(String studentId) {
        return "计算机科学与技术"; // 实际应从数据库查询
    }
    
    private static void initSampleData() {
        // 创建示例双导师课堂
        DualMentorCourse course1 = new DualMentorCourse();
        course1.id = "DMC-001";
        course1.courseCode = "CS301";
        course1.courseName = "企业级软件开发实践";
        course1.description = "结合理论与实践，学习企业级软件开发技术和流程";
        course1.universityId = "U-001";
        course1.universityName = "清华大学";
        course1.companyId = "C-001";
        course1.companyName = "腾讯科技";
        course1.universityTeacherId = "T-001";
        course1.universityTeacherName = "张教授";
        course1.enterpriseMentorId = "M-001";
        course1.enterpriseMentorName = "李导师";
        course1.department = "计算机系";
        course1.majorId = "MAJ-001";
        course1.majorName = "计算机科学与技术";
        course1.semester = "2024春";
        course1.academicYear = "2023-2024";
        course1.credits = 3;
        course1.totalHours = 48;
        course1.theoryHours = 24;
        course1.practiceHours = 24;
        course1.maxStudents = 30;
        course1.enrolledStudents = 25;
        course1.courseType = CourseType.MIXED;
        course1.teachingMode = TeachingMode.HYBRID;
        course1.status = CourseStatus.IN_PROGRESS;
        course1.startDate = LocalDate.of(2024, 2, 26);
        course1.endDate = LocalDate.of(2024, 6, 30);
        course1.createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        course1.updatedAt = LocalDateTime.now();
        COURSES.put(course1.id, course1);
        
        // 创建示例选课记录
        CourseEnrollment enrollment1 = new CourseEnrollment();
        enrollment1.id = "CE-001";
        enrollment1.courseId = "DMC-001";
        enrollment1.courseName = "企业级软件开发实践";
        enrollment1.studentId = "S-001";
        enrollment1.studentName = "张三";
        enrollment1.studentMajor = "计算机科学与技术";
        enrollment1.enrollmentDate = LocalDate.of(2024, 2, 1);
        enrollment1.status = EnrollmentStatus.ENROLLED;
        enrollment1.finalScore = 88.5;
        enrollment1.finalGrade = "B+";
        enrollment1.theoryScore = 85.0;
        enrollment1.practiceScore = 92.0;
        enrollment1.attendanceRate = 95.0;
        enrollment1.createdAt = LocalDateTime.of(2024, 2, 1, 14, 30);
        enrollment1.updatedAt = LocalDateTime.now();
        ENROLLMENTS.put(enrollment1.id, enrollment1);
        
        // 创建示例企业导师
        EnterpriseMentor mentor1 = new EnterpriseMentor();
        mentor1.id = "EM-001";
        mentor1.userId = "M-001";
        mentor1.name = "李导师";
        mentor1.email = "li.mentor@tencent.com";
        mentor1.phone = "138****5678";
        mentor1.companyId = "C-001";
        mentor1.companyName = "腾讯科技";
        mentor1.position = "高级技术专家";
        mentor1.department = "技术工程事业群";
        mentor1.workYears = 8;
        mentor1.expertise = "分布式系统、微服务架构、云原生技术";
        mentor1.education = "清华大学计算机科学与技术博士";
        mentor1.certifications = "AWS认证解决方案架构师、Kubernetes认证管理员";
        mentor1.introduction = "专注于大规模分布式系统设计与实现，有丰富的企业级项目经验";
        mentor1.status = MentorStatus.ACTIVE;
        mentor1.createdAt = LocalDateTime.of(2023, 9, 1, 9, 0);
        mentor1.updatedAt = LocalDateTime.now();
        MENTORS.put(mentor1.id, mentor1);
    }
}
