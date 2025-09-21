package com.minib.internships;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.minib.internships.InternshipDtos.*;

@RestController
@RequestMapping("/api/internships")
@CrossOrigin(origins = "*")
public class InternshipController {
    
    // 模拟数据存储
    private static final Map<String, InternshipPosition> POSITIONS = new ConcurrentHashMap<>();
    private static final Map<String, InternshipApplication> APPLICATIONS = new ConcurrentHashMap<>();
    private static final Map<String, InternshipRecord> RECORDS = new ConcurrentHashMap<>();
    
    static {
        // 初始化示例数据
        initSampleData();
    }
    
    // ================================
    // 实习职位管理
    // ================================
    
    @GetMapping("/positions")
    public ResponseEntity<PageResult<InternshipPosition>> getPositions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) PositionType type,
            @RequestParam(required = false) PositionStatus status,
            @RequestParam(required = false) String location) {
        
        List<InternshipPosition> filtered = POSITIONS.values().stream()
                .filter(p -> search == null || p.title.contains(search) || p.description.contains(search))
                .filter(p -> companyId == null || p.companyId.equals(companyId))
                .filter(p -> type == null || p.positionType == type)
                .filter(p -> status == null || p.status == status)
                .filter(p -> location == null || p.location.contains(location))
                .sorted((a, b) -> b.createdAt.compareTo(a.createdAt))
                .collect(Collectors.toList());
        
        int total = filtered.size();
        List<InternshipPosition> items = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        
        // 设置申请数量
        items.forEach(position -> {
            position.applicationCount = (int) APPLICATIONS.values().stream()
                    .filter(app -> app.positionId.equals(position.id))
                    .count();
        });
        
        PageResult<InternshipPosition> result = new PageResult<>();
        result.items = items;
        result.total = total;
        result.page = page;
        result.size = size;
        result.totalPages = (total + size - 1) / size;
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/positions/{id}")
    public ResponseEntity<InternshipPosition> getPosition(@PathVariable String id) {
        InternshipPosition position = POSITIONS.get(id);
        if (position == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 加载申请信息
        position.applications = APPLICATIONS.values().stream()
                .filter(app -> app.positionId.equals(id))
                .sorted((a, b) -> b.appliedAt.compareTo(a.appliedAt))
                .collect(Collectors.toList());
        position.applicationCount = position.applications.size();
        
        return ResponseEntity.ok(position);
    }
    
    @PostMapping("/positions")
    public ResponseEntity<InternshipPosition> createPosition(@Validated @RequestBody CreatePositionRequest request) {
        InternshipPosition position = new InternshipPosition();
        position.id = UUID.randomUUID().toString();
        position.companyId = "current-company-id"; // 实际应从认证信息获取
        position.companyName = getCompanyName(position.companyId);
        position.title = request.title;
        position.description = request.description;
        position.department = request.department;
        position.location = request.location;
        position.positionType = request.positionType;
        position.durationMonths = request.durationMonths;
        position.salaryMin = request.salaryMin;
        position.salaryMax = request.salaryMax;
        position.currency = request.currency;
        position.requirements = request.requirements;
        position.responsibilities = request.responsibilities;
        position.skillsRequired = request.skillsRequired;
        position.majorPreferred = request.majorPreferred;
        position.gradeRequirement = request.gradeRequirement;
        position.positionsAvailable = request.positionsAvailable;
        position.positionsFilled = 0;
        position.applicationDeadline = request.applicationDeadline;
        position.startDate = request.startDate;
        position.endDate = request.endDate;
        position.status = PositionStatus.DRAFT;
        position.createdBy = "current-user-id";
        position.createdByName = "当前用户";
        position.createdAt = LocalDateTime.now();
        position.updatedAt = LocalDateTime.now();
        position.applicationCount = 0;
        position.applications = new ArrayList<>();
        
        POSITIONS.put(position.id, position);
        return ResponseEntity.ok(position);
    }
    
    @PutMapping("/positions/{id}")
    public ResponseEntity<InternshipPosition> updatePosition(@PathVariable String id,
                                                           @Validated @RequestBody UpdatePositionRequest request) {
        InternshipPosition position = POSITIONS.get(id);
        if (position == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.title != null) position.title = request.title;
        if (request.description != null) position.description = request.description;
        if (request.department != null) position.department = request.department;
        if (request.location != null) position.location = request.location;
        if (request.positionType != null) position.positionType = request.positionType;
        if (request.durationMonths != null) position.durationMonths = request.durationMonths;
        if (request.salaryMin != null) position.salaryMin = request.salaryMin;
        if (request.salaryMax != null) position.salaryMax = request.salaryMax;
        if (request.currency != null) position.currency = request.currency;
        if (request.requirements != null) position.requirements = request.requirements;
        if (request.responsibilities != null) position.responsibilities = request.responsibilities;
        if (request.skillsRequired != null) position.skillsRequired = request.skillsRequired;
        if (request.majorPreferred != null) position.majorPreferred = request.majorPreferred;
        if (request.gradeRequirement != null) position.gradeRequirement = request.gradeRequirement;
        if (request.positionsAvailable != null) position.positionsAvailable = request.positionsAvailable;
        if (request.applicationDeadline != null) position.applicationDeadline = request.applicationDeadline;
        if (request.startDate != null) position.startDate = request.startDate;
        if (request.endDate != null) position.endDate = request.endDate;
        if (request.status != null) position.status = request.status;
        
        position.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(position);
    }
    
    @DeleteMapping("/positions/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable String id) {
        InternshipPosition position = POSITIONS.remove(id);
        if (position == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 删除相关申请
        APPLICATIONS.values().removeIf(app -> app.positionId.equals(id));
        
        return ResponseEntity.noContent().build();
    }
    
    // ================================
    // 实习申请管理
    // ================================
    
    @GetMapping("/applications")
    public ResponseEntity<PageResult<InternshipApplication>> getApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String positionId,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) ApplicationStatus status) {
        
        List<InternshipApplication> filtered = APPLICATIONS.values().stream()
                .filter(app -> positionId == null || app.positionId.equals(positionId))
                .filter(app -> studentId == null || app.studentId.equals(studentId))
                .filter(app -> status == null || app.status == status)
                .sorted((a, b) -> b.appliedAt.compareTo(a.appliedAt))
                .collect(Collectors.toList());
        
        int total = filtered.size();
        List<InternshipApplication> items = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        
        PageResult<InternshipApplication> result = new PageResult<>();
        result.items = items;
        result.total = total;
        result.page = page;
        result.size = size;
        result.totalPages = (total + size - 1) / size;
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/applications/{id}")
    public ResponseEntity<InternshipApplication> getApplication(@PathVariable String id) {
        InternshipApplication application = APPLICATIONS.get(id);
        if (application == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(application);
    }
    
    @PostMapping("/positions/{positionId}/apply")
    public ResponseEntity<InternshipApplication> applyInternship(@PathVariable String positionId,
                                                               @Validated @RequestBody ApplyInternshipRequest request) {
        InternshipPosition position = POSITIONS.get(positionId);
        if (position == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (position.status != PositionStatus.PUBLISHED) {
            return ResponseEntity.badRequest().build();
        }
        
        String studentId = "current-student-id"; // 实际应从认证信息获取
        
        // 检查是否已申请
        boolean alreadyApplied = APPLICATIONS.values().stream()
                .anyMatch(app -> app.positionId.equals(positionId) && app.studentId.equals(studentId));
        if (alreadyApplied) {
            return ResponseEntity.badRequest().build();
        }
        
        InternshipApplication application = new InternshipApplication();
        application.id = UUID.randomUUID().toString();
        application.positionId = positionId;
        application.positionTitle = position.title;
        application.companyName = position.companyName;
        application.studentId = studentId;
        application.studentName = getStudentName(studentId);
        application.studentEmail = getStudentEmail(studentId);
        application.studentPhone = getStudentPhone(studentId);
        application.studentMajor = getStudentMajor(studentId);
        application.studentGrade = getStudentGrade(studentId);
        application.coverLetter = request.coverLetter;
        application.resumeUrl = request.resumeUrl;
        application.portfolioUrl = request.portfolioUrl;
        application.gpa = request.gpa;
        application.skills = request.skills;
        application.experience = request.experience;
        application.status = ApplicationStatus.SUBMITTED;
        application.appliedAt = LocalDateTime.now();
        application.updatedAt = LocalDateTime.now();
        
        APPLICATIONS.put(application.id, application);
        return ResponseEntity.ok(application);
    }
    
    @PutMapping("/applications/{id}")
    public ResponseEntity<InternshipApplication> updateApplication(@PathVariable String id,
                                                                 @Validated @RequestBody UpdateApplicationRequest request) {
        InternshipApplication application = APPLICATIONS.get(id);
        if (application == null) {
            return ResponseEntity.notFound().build();
        }
        
        application.status = request.status;
        if (request.interviewDate != null) application.interviewDate = request.interviewDate;
        if (request.interviewFeedback != null) application.interviewFeedback = request.interviewFeedback;
        if (request.rejectionReason != null) application.rejectionReason = request.rejectionReason;
        
        application.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(application);
    }
    
    // ================================
    // 实习记录管理
    // ================================
    
    @GetMapping("/records")
    public ResponseEntity<PageResult<InternshipRecord>> getRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) RecordStatus status) {
        
        List<InternshipRecord> filtered = RECORDS.values().stream()
                .filter(record -> studentId == null || record.studentId.equals(studentId))
                .filter(record -> companyId == null || record.companyId.equals(companyId))
                .filter(record -> status == null || record.status == status)
                .sorted((a, b) -> b.createdAt.compareTo(a.createdAt))
                .collect(Collectors.toList());
        
        int total = filtered.size();
        List<InternshipRecord> items = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        
        PageResult<InternshipRecord> result = new PageResult<>();
        result.items = items;
        result.total = total;
        result.page = page;
        result.size = size;
        result.totalPages = (total + size - 1) / size;
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/records/{id}")
    public ResponseEntity<InternshipRecord> getRecord(@PathVariable String id) {
        InternshipRecord record = RECORDS.get(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }
    
    @PostMapping("/records")
    public ResponseEntity<InternshipRecord> createRecord(@Validated @RequestBody CreateRecordRequest request) {
        InternshipApplication application = APPLICATIONS.get(request.applicationId);
        if (application == null || application.status != ApplicationStatus.ACCEPTED) {
            return ResponseEntity.badRequest().build();
        }
        
        InternshipPosition position = POSITIONS.get(application.positionId);
        if (position == null) {
            return ResponseEntity.badRequest().build();
        }
        
        InternshipRecord record = new InternshipRecord();
        record.id = UUID.randomUUID().toString();
        record.applicationId = request.applicationId;
        record.studentId = application.studentId;
        record.studentName = application.studentName;
        record.companyId = position.companyId;
        record.companyName = position.companyName;
        record.positionTitle = position.title;
        record.mentorId = request.mentorId;
        record.mentorName = request.mentorId != null ? getUserName(request.mentorId) : null;
        record.supervisorId = request.supervisorId;
        record.supervisorName = request.supervisorId != null ? getUserName(request.supervisorId) : null;
        record.startDate = request.startDate;
        record.endDate = request.endDate;
        record.department = request.department != null ? request.department : position.department;
        record.location = request.location != null ? request.location : position.location;
        record.salary = request.salary;
        record.status = RecordStatus.ONGOING;
        record.createdAt = LocalDateTime.now();
        record.updatedAt = LocalDateTime.now();
        
        RECORDS.put(record.id, record);
        
        // 更新职位已填充数量
        position.positionsFilled = position.positionsFilled + 1;
        
        return ResponseEntity.ok(record);
    }
    
    @PutMapping("/records/{id}")
    public ResponseEntity<InternshipRecord> updateRecord(@PathVariable String id,
                                                        @Validated @RequestBody UpdateRecordRequest request) {
        InternshipRecord record = RECORDS.get(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.actualEndDate != null) record.actualEndDate = request.actualEndDate;
        if (request.status != null) record.status = request.status;
        if (request.performanceScore != null) record.performanceScore = request.performanceScore;
        if (request.mentorFeedback != null) record.mentorFeedback = request.mentorFeedback;
        if (request.supervisorFeedback != null) record.supervisorFeedback = request.supervisorFeedback;
        if (request.studentFeedback != null) record.studentFeedback = request.studentFeedback;
        if (request.certificateUrl != null) record.certificateUrl = request.certificateUrl;
        
        record.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(record);
    }
    
    // ================================
    // 统计信息
    // ================================
    
    @GetMapping("/stats")
    public ResponseEntity<InternshipStats> getInternshipStats() {
        InternshipStats stats = new InternshipStats();
        
        List<InternshipPosition> allPositions = new ArrayList<>(POSITIONS.values());
        List<InternshipApplication> allApplications = new ArrayList<>(APPLICATIONS.values());
        List<InternshipRecord> allRecords = new ArrayList<>(RECORDS.values());
        
        stats.totalPositions = allPositions.size();
        stats.activePositions = allPositions.stream()
                .filter(p -> p.status == PositionStatus.PUBLISHED)
                .count();
        stats.totalApplications = allApplications.size();
        stats.acceptedApplications = allApplications.stream()
                .filter(app -> app.status == ApplicationStatus.ACCEPTED)
                .count();
        stats.ongoingInternships = allRecords.stream()
                .filter(r -> r.status == RecordStatus.ONGOING)
                .count();
        stats.completedInternships = allRecords.stream()
                .filter(r -> r.status == RecordStatus.COMPLETED)
                .count();
        
        // 计算平均薪资
        OptionalDouble avgSalary = allPositions.stream()
                .filter(p -> p.salaryMin != null && p.salaryMax != null)
                .mapToDouble(p -> (p.salaryMin.doubleValue() + p.salaryMax.doubleValue()) / 2)
                .average();
        stats.averageSalary = avgSalary.isPresent() ? BigDecimal.valueOf(avgSalary.getAsDouble()) : BigDecimal.ZERO;
        
        // 企业统计
        stats.topCompanies = allPositions.stream()
                .collect(Collectors.groupingBy(p -> p.companyId, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CompanyStats companyStats = new CompanyStats();
                    companyStats.companyId = entry.getKey();
                    companyStats.companyName = entry.getValue().get(0).companyName;
                    companyStats.positionCount = entry.getValue().size();
                    companyStats.applicationCount = allApplications.stream()
                            .filter(app -> entry.getValue().stream()
                                    .anyMatch(pos -> pos.id.equals(app.positionId)))
                            .count();
                    return companyStats;
                })
                .sorted((a, b) -> Long.compare(b.positionCount, a.positionCount))
                .limit(10)
                .collect(Collectors.toList());
        
        // 专业统计
        stats.topMajors = allApplications.stream()
                .filter(app -> app.studentMajor != null)
                .collect(Collectors.groupingBy(app -> app.studentMajor, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    MajorStats majorStats = new MajorStats();
                    majorStats.major = entry.getKey();
                    majorStats.applicationCount = entry.getValue().size();
                    majorStats.acceptedCount = entry.getValue().stream()
                            .filter(app -> app.status == ApplicationStatus.ACCEPTED)
                            .count();
                    return majorStats;
                })
                .sorted((a, b) -> Long.compare(b.applicationCount, a.applicationCount))
                .limit(10)
                .collect(Collectors.toList());
        
        // 类型统计
        stats.typeStats = Arrays.stream(PositionType.values())
                .map(type -> {
                    PositionTypeStats typeStats = new PositionTypeStats();
                    typeStats.type = type;
                    typeStats.count = allPositions.stream()
                            .filter(p -> p.positionType == type)
                            .count();
                    OptionalDouble typeAvgSalary = allPositions.stream()
                            .filter(p -> p.positionType == type && p.salaryMin != null && p.salaryMax != null)
                            .mapToDouble(p -> (p.salaryMin.doubleValue() + p.salaryMax.doubleValue()) / 2)
                            .average();
                    typeStats.averageSalary = typeAvgSalary.isPresent() ? 
                            BigDecimal.valueOf(typeAvgSalary.getAsDouble()) : BigDecimal.ZERO;
                    return typeStats;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(stats);
    }
    
    // ================================
    // 辅助方法
    // ================================
    
    private String getCompanyName(String companyId) {
        return "示例企业"; // 实际应从数据库查询
    }
    
    private String getStudentName(String studentId) {
        return "学生" + studentId.substring(0, 8); // 实际应从数据库查询
    }
    
    private String getStudentEmail(String studentId) {
        return "student" + studentId.substring(0, 8) + "@university.edu"; // 实际应从数据库查询
    }
    
    private String getStudentPhone(String studentId) {
        return "138****" + studentId.substring(0, 4); // 实际应从数据库查询
    }
    
    private String getStudentMajor(String studentId) {
        return "计算机科学与技术"; // 实际应从数据库查询
    }
    
    private String getStudentGrade(String studentId) {
        return "大三"; // 实际应从数据库查询
    }
    
    private String getUserName(String userId) {
        return "用户" + userId.substring(0, 8); // 实际应从数据库查询
    }
    
    private static void initSampleData() {
        // 创建示例实习职位
        InternshipPosition position1 = new InternshipPosition();
        position1.id = "IP-001";
        position1.companyId = "C-001";
        position1.companyName = "腾讯科技";
        position1.title = "前端开发实习生";
        position1.description = "参与公司产品前端开发，学习最新的Web技术";
        position1.department = "技术部";
        position1.location = "深圳";
        position1.positionType = PositionType.FULL_TIME;
        position1.durationMonths = 6;
        position1.salaryMin = new BigDecimal("4000");
        position1.salaryMax = new BigDecimal("6000");
        position1.currency = "CNY";
        position1.requirements = "计算机相关专业，熟悉HTML/CSS/JavaScript";
        position1.responsibilities = "参与前端页面开发，协助产品功能实现";
        position1.skillsRequired = Arrays.asList("JavaScript", "Vue.js", "CSS", "HTML");
        position1.majorPreferred = Arrays.asList("计算机科学与技术", "软件工程", "信息管理");
        position1.gradeRequirement = GradeRequirement.JUNIOR;
        position1.positionsAvailable = 5;
        position1.positionsFilled = 2;
        position1.applicationDeadline = LocalDate.of(2024, 3, 31);
        position1.startDate = LocalDate.of(2024, 4, 15);
        position1.endDate = LocalDate.of(2024, 10, 15);
        position1.status = PositionStatus.PUBLISHED;
        position1.createdBy = "company-admin-001";
        position1.createdByName = "HR经理";
        position1.createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        position1.updatedAt = LocalDateTime.now();
        POSITIONS.put(position1.id, position1);
        
        InternshipPosition position2 = new InternshipPosition();
        position2.id = "IP-002";
        position2.companyId = "C-002";
        position2.companyName = "阿里巴巴";
        position2.title = "数据分析实习生";
        position2.description = "参与用户行为数据分析，支持产品决策";
        position2.department = "数据部";
        position2.location = "杭州";
        position2.positionType = PositionType.FULL_TIME;
        position2.durationMonths = 4;
        position2.salaryMin = new BigDecimal("5000");
        position2.salaryMax = new BigDecimal("7000");
        position2.currency = "CNY";
        position2.requirements = "统计学、数学或相关专业，熟悉Python/R";
        position2.responsibilities = "数据清洗、分析建模、报告撰写";
        position2.skillsRequired = Arrays.asList("Python", "SQL", "统计学", "数据挖掘");
        position2.majorPreferred = Arrays.asList("统计学", "数据科学", "应用数学");
        position2.gradeRequirement = GradeRequirement.SENIOR;
        position2.positionsAvailable = 3;
        position2.positionsFilled = 0;
        position2.applicationDeadline = LocalDate.of(2024, 4, 15);
        position2.startDate = LocalDate.of(2024, 5, 1);
        position2.endDate = LocalDate.of(2024, 8, 31);
        position2.status = PositionStatus.PUBLISHED;
        position2.createdBy = "company-admin-002";
        position2.createdByName = "部门经理";
        position2.createdAt = LocalDateTime.of(2024, 2, 1, 14, 30);
        position2.updatedAt = LocalDateTime.now();
        POSITIONS.put(position2.id, position2);
        
        // 创建示例申请
        InternshipApplication application1 = new InternshipApplication();
        application1.id = "IA-001";
        application1.positionId = "IP-001";
        application1.positionTitle = "前端开发实习生";
        application1.companyName = "腾讯科技";
        application1.studentId = "S-001";
        application1.studentName = "张三";
        application1.studentEmail = "zhangsan@university.edu";
        application1.studentPhone = "138****1234";
        application1.studentMajor = "计算机科学与技术";
        application1.studentGrade = "大三";
        application1.coverLetter = "我对前端开发非常感兴趣，希望能在贵公司学习实践";
        application1.resumeUrl = "https://example.com/resume.pdf";
        application1.gpa = new BigDecimal("3.8");
        application1.skills = Arrays.asList("JavaScript", "Vue.js", "React", "Node.js");
        application1.experience = "参与过学校项目开发，有一定的前端开发经验";
        application1.status = ApplicationStatus.ACCEPTED;
        application1.appliedAt = LocalDateTime.of(2024, 2, 15, 16, 30);
        application1.updatedAt = LocalDateTime.of(2024, 2, 20, 10, 15);
        APPLICATIONS.put(application1.id, application1);
        
        // 创建示例实习记录
        InternshipRecord record1 = new InternshipRecord();
        record1.id = "IR-001";
        record1.applicationId = "IA-001";
        record1.studentId = "S-001";
        record1.studentName = "张三";
        record1.companyId = "C-001";
        record1.companyName = "腾讯科技";
        record1.positionTitle = "前端开发实习生";
        record1.mentorId = "M-001";
        record1.mentorName = "李导师";
        record1.supervisorId = "T-001";
        record1.supervisorName = "王教授";
        record1.startDate = LocalDate.of(2024, 4, 15);
        record1.endDate = LocalDate.of(2024, 10, 15);
        record1.department = "技术部";
        record1.location = "深圳";
        record1.salary = new BigDecimal("5000");
        record1.status = RecordStatus.ONGOING;
        record1.performanceScore = new BigDecimal("4.5");
        record1.mentorFeedback = "学习能力强，工作积极主动";
        record1.createdAt = LocalDateTime.of(2024, 4, 15, 9, 0);
        record1.updatedAt = LocalDateTime.now();
        RECORDS.put(record1.id, record1);
    }
}
