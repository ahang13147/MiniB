package com.minib.projects;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.minib.projects.ProjectDtos.*;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {
    
    // 模拟数据存储
    private static final Map<String, Project> PROJECTS = new ConcurrentHashMap<>();
    private static final Map<String, ProjectParticipant> PARTICIPANTS = new ConcurrentHashMap<>();
    private static final Map<String, ProjectMilestone> MILESTONES = new ConcurrentHashMap<>();
    private static final Map<String, ProjectFund> FUNDS = new ConcurrentHashMap<>();
    
    static {
        // 初始化示例数据
        initSampleData();
    }
    
    // ================================
    // 项目基本管理
    // ================================
    
    @GetMapping
    public ResponseEntity<PageResult<Project>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProjectType type,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String initiatorType) {
        
        List<Project> filtered = PROJECTS.values().stream()
                .filter(p -> search == null || p.title.contains(search) || p.description.contains(search))
                .filter(p -> type == null || p.projectType == type)
                .filter(p -> status == null || p.status == status)
                .filter(p -> initiatorType == null || p.initiatorType.toString().equals(initiatorType))
                .sorted((a, b) -> b.createdAt.compareTo(a.createdAt))
                .collect(Collectors.toList());
        
        int total = filtered.size();
        List<Project> items = filtered.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        
        PageResult<Project> result = new PageResult<>();
        result.items = items;
        result.total = total;
        result.page = page;
        result.size = size;
        result.totalPages = (total + size - 1) / size;
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable String id) {
        Project project = PROJECTS.get(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 加载参与人员和里程碑
        project.participants = getProjectParticipantsList(id);
        project.milestones = getProjectMilestonesList(id);
        project.participantCount = project.participants.size();
        project.milestoneCount = project.milestones.size();
        
        return ResponseEntity.ok(project);
    }
    
    @PostMapping
    public ResponseEntity<Project> createProject(@Validated @RequestBody CreateProjectRequest request) {
        Project project = new Project();
        project.id = UUID.randomUUID().toString();
        project.title = request.title;
        project.description = request.description;
        project.projectType = request.projectType;
        project.initiatorType = request.initiatorType;
        project.initiatorId = request.initiatorId;
        project.initiatorName = getEntityName(request.initiatorType, request.initiatorId);
        project.partnerType = request.partnerType;
        project.partnerId = request.partnerId;
        project.partnerName = request.partnerId != null ? getEntityName(request.partnerType, request.partnerId) : null;
        project.status = ProjectStatus.DRAFT;
        project.priority = request.priority;
        project.budget = request.budget;
        project.currency = request.currency;
        project.startDate = request.startDate;
        project.endDate = request.endDate;
        project.tags = request.tags;
        project.requirements = request.requirements;
        project.deliverables = request.deliverables;
        project.progressPercentage = 0;
        project.createdBy = "current-user-id"; // 实际应从认证信息获取
        project.createdByName = "当前用户";
        project.createdAt = LocalDateTime.now();
        project.updatedAt = LocalDateTime.now();
        project.participants = new ArrayList<>();
        project.milestones = new ArrayList<>();
        project.participantCount = 0;
        project.milestoneCount = 0;
        
        PROJECTS.put(project.id, project);
        return ResponseEntity.ok(project);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable String id, 
                                                @Validated @RequestBody UpdateProjectRequest request) {
        Project project = PROJECTS.get(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.title != null) project.title = request.title;
        if (request.description != null) project.description = request.description;
        if (request.projectType != null) project.projectType = request.projectType;
        if (request.partnerType != null) project.partnerType = request.partnerType;
        if (request.partnerId != null) {
            project.partnerId = request.partnerId;
            project.partnerName = getEntityName(request.partnerType, request.partnerId);
        }
        if (request.status != null) project.status = request.status;
        if (request.priority != null) project.priority = request.priority;
        if (request.budget != null) project.budget = request.budget;
        if (request.currency != null) project.currency = request.currency;
        if (request.startDate != null) project.startDate = request.startDate;
        if (request.endDate != null) project.endDate = request.endDate;
        if (request.actualStartDate != null) project.actualStartDate = request.actualStartDate;
        if (request.actualEndDate != null) project.actualEndDate = request.actualEndDate;
        if (request.tags != null) project.tags = request.tags;
        if (request.requirements != null) project.requirements = request.requirements;
        if (request.deliverables != null) project.deliverables = request.deliverables;
        if (request.progressPercentage != null) project.progressPercentage = request.progressPercentage;
        
        project.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(project);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        Project project = PROJECTS.remove(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 删除相关数据
        PARTICIPANTS.values().removeIf(p -> p.projectId.equals(id));
        MILESTONES.values().removeIf(m -> m.projectId.equals(id));
        FUNDS.values().removeIf(f -> f.projectId.equals(id));
        
        return ResponseEntity.noContent().build();
    }
    
    // ================================
    // 项目参与人员管理
    // ================================
    
    @GetMapping("/{projectId}/participants")
    public ResponseEntity<List<ProjectParticipant>> getProjectParticipants(@PathVariable String projectId) {
        List<ProjectParticipant> participants = PARTICIPANTS.values().stream()
                .filter(p -> p.projectId.equals(projectId))
                .sorted((a, b) -> a.joinDate.compareTo(b.joinDate))
                .collect(Collectors.toList());
        return ResponseEntity.ok(participants);
    }
    
    @PostMapping("/{projectId}/participants")
    public ResponseEntity<ProjectParticipant> addParticipant(@PathVariable String projectId,
                                                           @Validated @RequestBody AddParticipantRequest request) {
        if (!PROJECTS.containsKey(projectId)) {
            return ResponseEntity.notFound().build();
        }
        
        ProjectParticipant participant = new ProjectParticipant();
        participant.id = UUID.randomUUID().toString();
        participant.projectId = projectId;
        participant.userId = request.userId;
        participant.userName = getUserName(request.userId);
        participant.userRole = getUserRole(request.userId);
        participant.participantRole = request.participantRole;
        participant.joinDate = LocalDate.now();
        participant.status = ParticipantStatus.ACTIVE;
        participant.contributionPercentage = request.contributionPercentage;
        participant.createdAt = LocalDateTime.now();
        
        PARTICIPANTS.put(participant.id, participant);
        return ResponseEntity.ok(participant);
    }
    
    @PutMapping("/{projectId}/participants/{participantId}")
    public ResponseEntity<ProjectParticipant> updateParticipant(@PathVariable String projectId,
                                                              @PathVariable String participantId,
                                                              @Validated @RequestBody UpdateParticipantRequest request) {
        ProjectParticipant participant = PARTICIPANTS.get(participantId);
        if (participant == null || !participant.projectId.equals(projectId)) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.participantRole != null) participant.participantRole = request.participantRole;
        if (request.status != null) participant.status = request.status;
        if (request.contributionPercentage != null) participant.contributionPercentage = request.contributionPercentage;
        
        return ResponseEntity.ok(participant);
    }
    
    @DeleteMapping("/{projectId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable String projectId,
                                                 @PathVariable String participantId) {
        ProjectParticipant participant = PARTICIPANTS.get(participantId);
        if (participant == null || !participant.projectId.equals(projectId)) {
            return ResponseEntity.notFound().build();
        }
        
        PARTICIPANTS.remove(participantId);
        return ResponseEntity.noContent().build();
    }
    
    // ================================
    // 项目里程碑管理
    // ================================
    
    @GetMapping("/{projectId}/milestones")
    public ResponseEntity<List<ProjectMilestone>> getProjectMilestones(@PathVariable String projectId) {
        List<ProjectMilestone> milestones = MILESTONES.values().stream()
                .filter(m -> m.projectId.equals(projectId))
                .sorted((a, b) -> a.plannedDate.compareTo(b.plannedDate))
                .collect(Collectors.toList());
        return ResponseEntity.ok(milestones);
    }
    
    @PostMapping("/{projectId}/milestones")
    public ResponseEntity<ProjectMilestone> createMilestone(@PathVariable String projectId,
                                                          @Validated @RequestBody CreateMilestoneRequest request) {
        if (!PROJECTS.containsKey(projectId)) {
            return ResponseEntity.notFound().build();
        }
        
        ProjectMilestone milestone = new ProjectMilestone();
        milestone.id = UUID.randomUUID().toString();
        milestone.projectId = projectId;
        milestone.title = request.title;
        milestone.description = request.description;
        milestone.plannedDate = request.plannedDate;
        milestone.status = MilestoneStatus.PENDING;
        milestone.progressPercentage = 0;
        milestone.createdAt = LocalDateTime.now();
        milestone.updatedAt = LocalDateTime.now();
        
        MILESTONES.put(milestone.id, milestone);
        return ResponseEntity.ok(milestone);
    }
    
    @PutMapping("/{projectId}/milestones/{milestoneId}")
    public ResponseEntity<ProjectMilestone> updateMilestone(@PathVariable String projectId,
                                                          @PathVariable String milestoneId,
                                                          @Validated @RequestBody UpdateMilestoneRequest request) {
        ProjectMilestone milestone = MILESTONES.get(milestoneId);
        if (milestone == null || !milestone.projectId.equals(projectId)) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.title != null) milestone.title = request.title;
        if (request.description != null) milestone.description = request.description;
        if (request.plannedDate != null) milestone.plannedDate = request.plannedDate;
        if (request.actualDate != null) milestone.actualDate = request.actualDate;
        if (request.status != null) milestone.status = request.status;
        if (request.progressPercentage != null) milestone.progressPercentage = request.progressPercentage;
        
        milestone.updatedAt = LocalDateTime.now();
        
        return ResponseEntity.ok(milestone);
    }
    
    @DeleteMapping("/{projectId}/milestones/{milestoneId}")
    public ResponseEntity<Void> deleteMilestone(@PathVariable String projectId,
                                              @PathVariable String milestoneId) {
        ProjectMilestone milestone = MILESTONES.get(milestoneId);
        if (milestone == null || !milestone.projectId.equals(projectId)) {
            return ResponseEntity.notFound().build();
        }
        
        MILESTONES.remove(milestoneId);
        return ResponseEntity.noContent().build();
    }
    
    // ================================
    // 项目资金管理
    // ================================
    
    @GetMapping("/{projectId}/funds")
    public ResponseEntity<List<ProjectFund>> getProjectFunds(@PathVariable String projectId) {
        List<ProjectFund> funds = FUNDS.values().stream()
                .filter(f -> f.projectId.equals(projectId))
                .sorted((a, b) -> b.createdAt.compareTo(a.createdAt))
                .collect(Collectors.toList());
        return ResponseEntity.ok(funds);
    }
    
    @PostMapping("/{projectId}/funds")
    public ResponseEntity<ProjectFund> addFund(@PathVariable String projectId,
                                             @Validated @RequestBody AddFundRequest request) {
        if (!PROJECTS.containsKey(projectId)) {
            return ResponseEntity.notFound().build();
        }
        
        ProjectFund fund = new ProjectFund();
        fund.id = UUID.randomUUID().toString();
        fund.projectId = projectId;
        fund.fundType = request.fundType;
        fund.category = request.category;
        fund.amount = request.amount;
        fund.currency = request.currency;
        fund.description = request.description;
        fund.transactionDate = request.transactionDate != null ? request.transactionDate : LocalDate.now();
        fund.status = FundStatus.PLANNED;
        fund.createdBy = "current-user-id";
        fund.createdByName = "当前用户";
        fund.createdAt = LocalDateTime.now();
        
        FUNDS.put(fund.id, fund);
        return ResponseEntity.ok(fund);
    }
    
    // ================================
    // 项目统计信息
    // ================================
    
    @GetMapping("/stats")
    public ResponseEntity<ProjectStats> getProjectStats() {
        ProjectStats stats = new ProjectStats();
        
        List<Project> allProjects = new ArrayList<>(PROJECTS.values());
        stats.totalProjects = allProjects.size();
        stats.activeProjects = allProjects.stream()
                .filter(p -> p.status == ProjectStatus.IN_PROGRESS)
                .count();
        stats.completedProjects = allProjects.stream()
                .filter(p -> p.status == ProjectStatus.COMPLETED)
                .count();
        stats.myProjects = allProjects.stream()
                .filter(p -> "current-user-id".equals(p.createdBy))
                .count();
        
        stats.totalBudget = allProjects.stream()
                .filter(p -> p.budget != null)
                .map(p -> p.budget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.completedBudget = allProjects.stream()
                .filter(p -> p.status == ProjectStatus.COMPLETED && p.budget != null)
                .map(p -> p.budget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 类型统计
        stats.typeStats = Arrays.stream(ProjectType.values())
                .map(type -> {
                    ProjectTypeStats typeStats = new ProjectTypeStats();
                    typeStats.type = type;
                    typeStats.count = allProjects.stream()
                            .filter(p -> p.projectType == type)
                            .count();
                    typeStats.totalBudget = allProjects.stream()
                            .filter(p -> p.projectType == type && p.budget != null)
                            .map(p -> p.budget)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return typeStats;
                })
                .collect(Collectors.toList());
        
        // 状态统计
        stats.statusStats = Arrays.stream(ProjectStatus.values())
                .map(status -> {
                    ProjectStatusStats statusStats = new ProjectStatusStats();
                    statusStats.status = status;
                    statusStats.count = allProjects.stream()
                            .filter(p -> p.status == status)
                            .count();
                    statusStats.totalBudget = allProjects.stream()
                            .filter(p -> p.status == status && p.budget != null)
                            .map(p -> p.budget)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return statusStats;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(stats);
    }
    
    // ================================
    // 辅助方法
    // ================================
    
    private List<ProjectParticipant> getProjectParticipantsList(String projectId) {
        return PARTICIPANTS.values().stream()
                .filter(p -> p.projectId.equals(projectId))
                .sorted((a, b) -> a.joinDate.compareTo(b.joinDate))
                .collect(Collectors.toList());
    }
    
    private List<ProjectMilestone> getProjectMilestonesList(String projectId) {
        return MILESTONES.values().stream()
                .filter(m -> m.projectId.equals(projectId))
                .sorted((a, b) -> a.plannedDate.compareTo(b.plannedDate))
                .collect(Collectors.toList());
    }
    
    private String getEntityName(InitiatorType type, String id) {
        if (type == InitiatorType.UNIVERSITY) {
            return "示例大学"; // 实际应从数据库查询
        } else {
            return "示例企业"; // 实际应从数据库查询
        }
    }
    
    private String getEntityName(PartnerType type, String id) {
        if (type == PartnerType.UNIVERSITY) {
            return "示例大学"; // 实际应从数据库查询
        } else {
            return "示例企业"; // 实际应从数据库查询
        }
    }
    
    private String getUserName(String userId) {
        return "用户" + userId.substring(0, 8); // 实际应从数据库查询
    }
    
    private String getUserRole(String userId) {
        return "TEACHER"; // 实际应从数据库查询
    }
    
    private static void initSampleData() {
        // 创建示例项目
        Project project1 = new Project();
        project1.id = "P-001";
        project1.title = "人工智能教育平台开发";
        project1.description = "开发一个基于AI的个性化教育平台，提升学习效率";
        project1.projectType = ProjectType.DEVELOPMENT;
        project1.initiatorType = InitiatorType.UNIVERSITY;
        project1.initiatorId = "U-001";
        project1.initiatorName = "清华大学";
        project1.partnerType = PartnerType.COMPANY;
        project1.partnerId = "C-001";
        project1.partnerName = "腾讯科技";
        project1.status = ProjectStatus.IN_PROGRESS;
        project1.priority = ProjectPriority.HIGH;
        project1.budget = new BigDecimal("500000");
        project1.currency = "CNY";
        project1.startDate = LocalDate.of(2024, 1, 1);
        project1.endDate = LocalDate.of(2024, 12, 31);
        project1.actualStartDate = LocalDate.of(2024, 1, 15);
        project1.tags = Arrays.asList("AI", "教育", "平台开发");
        project1.requirements = "需要AI算法工程师、前端开发工程师、产品经理";
        project1.deliverables = "完整的AI教育平台系统，包含用户端和管理端";
        project1.progressPercentage = 45;
        project1.createdBy = "teacher-001";
        project1.createdByName = "张教授";
        project1.createdAt = LocalDateTime.of(2023, 12, 1, 10, 0);
        project1.updatedAt = LocalDateTime.now();
        PROJECTS.put(project1.id, project1);
        
        Project project2 = new Project();
        project2.id = "P-002";
        project2.title = "新材料研发合作";
        project2.description = "共同研发新型复合材料，应用于航空航天领域";
        project2.projectType = ProjectType.RESEARCH;
        project2.initiatorType = InitiatorType.COMPANY;
        project2.initiatorId = "C-002";
        project2.initiatorName = "中航工业";
        project2.partnerType = PartnerType.UNIVERSITY;
        project2.partnerId = "U-002";
        project2.partnerName = "北京理工大学";
        project2.status = ProjectStatus.PUBLISHED;
        project2.priority = ProjectPriority.URGENT;
        project2.budget = new BigDecimal("1200000");
        project2.currency = "CNY";
        project2.startDate = LocalDate.of(2024, 3, 1);
        project2.endDate = LocalDate.of(2025, 2, 28);
        project2.tags = Arrays.asList("新材料", "航空航天", "复合材料");
        project2.requirements = "材料科学专家、测试工程师、项目经理";
        project2.deliverables = "新材料配方、性能测试报告、应用方案";
        project2.progressPercentage = 0;
        project2.createdBy = "company-admin-001";
        project2.createdByName = "李经理";
        project2.createdAt = LocalDateTime.of(2023, 12, 15, 14, 30);
        project2.updatedAt = LocalDateTime.now();
        PROJECTS.put(project2.id, project2);
        
        // 添加参与人员示例数据
        ProjectParticipant participant1 = new ProjectParticipant();
        participant1.id = "PP-001";
        participant1.projectId = "P-001";
        participant1.userId = "teacher-001";
        participant1.userName = "张教授";
        participant1.userRole = "TEACHER";
        participant1.participantRole = ParticipantRole.LEADER;
        participant1.joinDate = LocalDate.of(2024, 1, 1);
        participant1.status = ParticipantStatus.ACTIVE;
        participant1.contributionPercentage = new BigDecimal("40");
        participant1.createdAt = LocalDateTime.of(2024, 1, 1, 9, 0);
        PARTICIPANTS.put(participant1.id, participant1);
        
        // 添加里程碑示例数据
        ProjectMilestone milestone1 = new ProjectMilestone();
        milestone1.id = "PM-001";
        milestone1.projectId = "P-001";
        milestone1.title = "需求分析完成";
        milestone1.description = "完成平台功能需求分析和技术方案设计";
        milestone1.plannedDate = LocalDate.of(2024, 2, 29);
        milestone1.actualDate = LocalDate.of(2024, 3, 5);
        milestone1.status = MilestoneStatus.COMPLETED;
        milestone1.progressPercentage = 100;
        milestone1.createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        milestone1.updatedAt = LocalDateTime.of(2024, 3, 5, 16, 30);
        MILESTONES.put(milestone1.id, milestone1);
    }
}
