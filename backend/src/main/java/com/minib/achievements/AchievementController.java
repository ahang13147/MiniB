package com.minib.achievements;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.minib.achievements.AchievementDtos.*;

@RestController
@RequestMapping("/api/achievements")
@CrossOrigin(origins = "*")
public class AchievementController {
    
    // 模拟数据存储
    private static final Map<String, StudentAchievement> ACHIEVEMENTS = new ConcurrentHashMap<>();
    
    static {
        // 初始化示例数据
        initSampleData();
    }
    
    // ================================
    // 成果管理
    // ================================
    
    @GetMapping
    public ResponseEntity<PageResult<StudentAchievement>> getAchievements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) AchievementType type,
            @RequestParam(required = false) AchievementLevel level,
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(required = false) Visibility visibility) {
        
        List<StudentAchievement> filtered = ACHIEVEMENTS.values().stream()
                .filter(a -> search == null || a.title.contains(search) || a.description.contains(search))
                .filter(a -> studentId == null || a.studentId.equals(studentId))
                .filter(a -> type == null || a.achievementType == type)
                .filter(a -> level == null || a.level == level)
                .filter(a -> status == null || a.status == status)
                .filter(a -> visibility == null || a.visibility == visibility)
                .sorted((a, b) -> b.achievementDate.compareTo(a.achievementDate))
                .collect(Collectors.toList());
        
        int total = filtered.size();
        List<StudentAchievement> items = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        
        PageResult<StudentAchievement> result = new PageResult<>();
        result.items = items;
        result.total = total;
        result.page = page;
        result.size = size;
        result.totalPages = (total + size - 1) / size;
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StudentAchievement> getAchievement(@PathVariable String id) {
        StudentAchievement achievement = ACHIEVEMENTS.get(id);
        if (achievement == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(achievement);
    }
    
    @PostMapping
    public ResponseEntity<StudentAchievement> createAchievement(@Validated @RequestBody CreateAchievementRequest request) {
        StudentAchievement achievement = new StudentAchievement();
        achievement.id = UUID.randomUUID().toString();
        achievement.studentId = "current-student-id"; // 实际应从认证信息获取
        achievement.studentName = getStudentName(achievement.studentId);
        achievement.studentMajor = getStudentMajor(achievement.studentId);
        achievement.studentGrade = getStudentGrade(achievement.studentId);
        achievement.title = request.title;
        achievement.description = request.description;
        achievement.achievementType = request.achievementType;
        achievement.category = request.category;
        achievement.level = request.level;
        achievement.awardRank = request.awardRank;
        achievement.organization = request.organization;
        achievement.achievementDate = request.achievementDate;
        achievement.certificateUrl = request.certificateUrl;
        achievement.attachmentUrls = request.attachmentUrls;
        achievement.visibility = request.visibility;
        achievement.status = VerificationStatus.DRAFT;
        achievement.createdAt = LocalDateTime.now();
        achievement.updatedAt = LocalDateTime.now();
        
        ACHIEVEMENTS.put(achievement.id, achievement);
        return ResponseEntity.ok(achievement);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<StudentAchievement> updateAchievement(@PathVariable String id,
                                                              @Validated @RequestBody UpdateAchievementRequest request) {
        StudentAchievement achievement = ACHIEVEMENTS.get(id);
        if (achievement == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.title != null) achievement.title = request.title;
        if (request.description != null) achievement.description = request.description;
        if (request.achievementType != null) achievement.achievementType = request.achievementType;
        if (request.category != null) achievement.category = request.category;
        if (request.level != null) achievement.level = request.level;
        if (request.awardRank != null) achievement.awardRank = request.awardRank;
        if (request.organization != null) achievement.organization = request.organization;
        if (request.achievementDate != null) achievement.achievementDate = request.achievementDate;
        if (request.certificateUrl != null) achievement.certificateUrl = request.certificateUrl;
        if (request.attachmentUrls != null) achievement.attachmentUrls = request.attachmentUrls;
        if (request.visibility != null) achievement.visibility = request.visibility;
        
        achievement.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(achievement);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAchievement(@PathVariable String id) {
        StudentAchievement achievement = ACHIEVEMENTS.remove(id);
        if (achievement == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
    
    // ================================
    // 成果验证
    // ================================
    
    @PostMapping("/{id}/verify")
    public ResponseEntity<StudentAchievement> verifyAchievement(@PathVariable String id,
                                                              @Validated @RequestBody VerifyAchievementRequest request) {
        StudentAchievement achievement = ACHIEVEMENTS.get(id);
        if (achievement == null) {
            return ResponseEntity.notFound().build();
        }
        
        achievement.status = request.status;
        achievement.verificationNotes = request.verificationNotes;
        achievement.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(achievement);
    }
    
    @PostMapping("/{id}/publish")
    public ResponseEntity<StudentAchievement> publishAchievement(@PathVariable String id) {
        StudentAchievement achievement = ACHIEVEMENTS.get(id);
        if (achievement == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (achievement.status != VerificationStatus.DRAFT) {
            return ResponseEntity.badRequest().build();
        }
        
        achievement.status = VerificationStatus.PUBLISHED;
        achievement.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(achievement);
    }
    
    // ================================
    // 学生成果展示
    // ================================
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentAchievement>> getStudentAchievements(@PathVariable String studentId) {
        List<StudentAchievement> achievements = ACHIEVEMENTS.values().stream()
                .filter(a -> a.studentId.equals(studentId))
                .filter(a -> a.status == VerificationStatus.VERIFIED || a.status == VerificationStatus.PUBLISHED)
                .sorted((a, b) -> b.achievementDate.compareTo(a.achievementDate))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(achievements);
    }
    
    @GetMapping("/public")
    public ResponseEntity<PageResult<StudentAchievement>> getPublicAchievements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AchievementType type,
            @RequestParam(required = false) AchievementLevel level) {
        
        List<StudentAchievement> filtered = ACHIEVEMENTS.values().stream()
                .filter(a -> a.visibility == Visibility.PUBLIC)
                .filter(a -> a.status == VerificationStatus.VERIFIED)
                .filter(a -> type == null || a.achievementType == type)
                .filter(a -> level == null || a.level == level)
                .sorted((a, b) -> b.achievementDate.compareTo(a.achievementDate))
                .collect(Collectors.toList());
        
        int total = filtered.size();
        List<StudentAchievement> items = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        
        PageResult<StudentAchievement> result = new PageResult<>();
        result.items = items;
        result.total = total;
        result.page = page;
        result.size = size;
        result.totalPages = (total + size - 1) / size;
        
        return ResponseEntity.ok(result);
    }
    
    // ================================
    // 统计信息
    // ================================
    
    @GetMapping("/stats")
    public ResponseEntity<AchievementStats> getAchievementStats() {
        AchievementStats stats = new AchievementStats();
        
        List<StudentAchievement> allAchievements = new ArrayList<>(ACHIEVEMENTS.values());
        
        stats.totalAchievements = allAchievements.size();
        stats.verifiedAchievements = allAchievements.stream()
                .filter(a -> a.status == VerificationStatus.VERIFIED)
                .count();
        stats.pendingVerification = allAchievements.stream()
                .filter(a -> a.status == VerificationStatus.PUBLISHED)
                .count();
        
        // 类型统计
        stats.typeStats = Arrays.stream(AchievementType.values())
                .map(type -> {
                    TypeStats typeStats = new TypeStats();
                    typeStats.type = type;
                    typeStats.count = allAchievements.stream()
                            .filter(a -> a.achievementType == type)
                            .count();
                    typeStats.verifiedCount = allAchievements.stream()
                            .filter(a -> a.achievementType == type && a.status == VerificationStatus.VERIFIED)
                            .count();
                    return typeStats;
                })
                .collect(Collectors.toList());
        
        // 级别统计
        stats.levelStats = Arrays.stream(AchievementLevel.values())
                .map(level -> {
                    LevelStats levelStats = new LevelStats();
                    levelStats.level = level;
                    levelStats.count = allAchievements.stream()
                            .filter(a -> a.level == level)
                            .count();
                    levelStats.verifiedCount = allAchievements.stream()
                            .filter(a -> a.level == level && a.status == VerificationStatus.VERIFIED)
                            .count();
                    return levelStats;
                })
                .collect(Collectors.toList());
        
        // 学生统计
        stats.topStudents = allAchievements.stream()
                .filter(a -> a.status == VerificationStatus.VERIFIED)
                .collect(Collectors.groupingBy(a -> a.studentId, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    StudentStats studentStats = new StudentStats();
                    studentStats.studentId = entry.getKey();
                    studentStats.studentName = entry.getValue().get(0).studentName;
                    studentStats.studentMajor = entry.getValue().get(0).studentMajor;
                    studentStats.achievementCount = entry.getValue().size();
                    studentStats.verifiedCount = entry.getValue().stream()
                            .filter(a -> a.status == VerificationStatus.VERIFIED)
                            .count();
                    return studentStats;
                })
                .sorted((a, b) -> Long.compare(b.verifiedCount, a.verifiedCount))
                .limit(10)
                .collect(Collectors.toList());
        
        // 分类统计
        stats.categoryStats = allAchievements.stream()
                .filter(a -> a.category != null && a.status == VerificationStatus.VERIFIED)
                .collect(Collectors.groupingBy(a -> a.category, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CategoryStats categoryStats = new CategoryStats();
                    categoryStats.category = entry.getKey();
                    categoryStats.count = entry.getValue().size();
                    categoryStats.verifiedCount = entry.getValue().stream()
                            .filter(a -> a.status == VerificationStatus.VERIFIED)
                            .count();
                    return categoryStats;
                })
                .sorted((a, b) -> Long.compare(b.verifiedCount, a.verifiedCount))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(stats);
    }
    
    // ================================
    // 辅助方法
    // ================================
    
    private String getStudentName(String studentId) {
        return "学生" + studentId.substring(0, 8); // 实际应从数据库查询
    }
    
    private String getStudentMajor(String studentId) {
        return "计算机科学与技术"; // 实际应从数据库查询
    }
    
    private String getStudentGrade(String studentId) {
        return "大三"; // 实际应从数据库查询
    }
    
    private static void initSampleData() {
        // 创建示例成果
        StudentAchievement achievement1 = new StudentAchievement();
        achievement1.id = "A-001";
        achievement1.studentId = "S-001";
        achievement1.studentName = "张三";
        achievement1.studentMajor = "计算机科学与技术";
        achievement1.studentGrade = "大三";
        achievement1.title = "全国大学生程序设计竞赛金奖";
        achievement1.description = "参加ACM-ICPC全国大学生程序设计竞赛，获得金奖";
        achievement1.achievementType = AchievementType.COMPETITION;
        achievement1.category = "程序设计竞赛";
        achievement1.level = AchievementLevel.NATIONAL;
        achievement1.awardRank = "金奖";
        achievement1.organization = "中国计算机学会";
        achievement1.achievementDate = LocalDate.of(2023, 11, 15);
        achievement1.certificateUrl = "https://example.com/certificate1.pdf";
        achievement1.attachmentUrls = Arrays.asList("https://example.com/photo1.jpg");
        achievement1.visibility = Visibility.PUBLIC;
        achievement1.status = VerificationStatus.VERIFIED;
        achievement1.verificationNotes = "证书真实有效";
        achievement1.createdAt = LocalDateTime.of(2023, 11, 20, 10, 0);
        achievement1.updatedAt = LocalDateTime.of(2023, 11, 25, 14, 30);
        ACHIEVEMENTS.put(achievement1.id, achievement1);
        
        StudentAchievement achievement2 = new StudentAchievement();
        achievement2.id = "A-002";
        achievement2.studentId = "S-002";
        achievement2.studentName = "李四";
        achievement2.studentMajor = "软件工程";
        achievement2.studentGrade = "大四";
        achievement2.title = "基于深度学习的图像识别系统";
        achievement2.description = "开发了一个基于深度学习的图像识别系统，准确率达到95%";
        achievement2.achievementType = AchievementType.PROJECT;
        achievement2.category = "人工智能项目";
        achievement2.level = AchievementLevel.SCHOOL;
        achievement2.awardRank = "优秀项目";
        achievement2.organization = "清华大学";
        achievement2.achievementDate = LocalDate.of(2023, 12, 1);
        achievement2.certificateUrl = "https://example.com/certificate2.pdf";
        achievement2.attachmentUrls = Arrays.asList(
                "https://example.com/demo.mp4",
                "https://example.com/code.zip"
        );
        achievement2.visibility = Visibility.UNIVERSITY;
        achievement2.status = VerificationStatus.VERIFIED;
        achievement2.verificationNotes = "项目技术含量高，创新性强";
        achievement2.createdAt = LocalDateTime.of(2023, 12, 5, 16, 0);
        achievement2.updatedAt = LocalDateTime.of(2023, 12, 10, 9, 15);
        ACHIEVEMENTS.put(achievement2.id, achievement2);
        
        StudentAchievement achievement3 = new StudentAchievement();
        achievement3.id = "A-003";
        achievement3.studentId = "S-001";
        achievement3.studentName = "张三";
        achievement3.studentMajor = "计算机科学与技术";
        achievement3.studentGrade = "大三";
        achievement3.title = "一种新型数据结构的设计与实现";
        achievement3.description = "设计并实现了一种新型数据结构，发表在核心期刊上";
        achievement3.achievementType = AchievementType.RESEARCH;
        achievement3.category = "学术论文";
        achievement3.level = AchievementLevel.NATIONAL;
        achievement3.awardRank = "第一作者";
        achievement3.organization = "计算机学报";
        achievement3.achievementDate = LocalDate.of(2024, 1, 15);
        achievement3.certificateUrl = "https://example.com/paper.pdf";
        achievement3.attachmentUrls = Arrays.asList("https://example.com/code-supplement.zip");
        achievement3.visibility = Visibility.PUBLIC;
        achievement3.status = VerificationStatus.PUBLISHED;
        achievement3.createdAt = LocalDateTime.of(2024, 1, 20, 11, 30);
        achievement3.updatedAt = LocalDateTime.of(2024, 1, 20, 11, 30);
        ACHIEVEMENTS.put(achievement3.id, achievement3);
    }
}
