package com.minib.projects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDtos {
    
    // 项目基本信息
    public static class Project {
        public String id;
        public String title;
        public String description;
        public ProjectType projectType;
        public InitiatorType initiatorType;
        public String initiatorId;
        public String initiatorName;
        public PartnerType partnerType;
        public String partnerId;
        public String partnerName;
        public ProjectStatus status;
        public ProjectPriority priority;
        public BigDecimal budget;
        public String currency;
        public LocalDate startDate;
        public LocalDate endDate;
        public LocalDate actualStartDate;
        public LocalDate actualEndDate;
        public List<String> tags;
        public String requirements;
        public String deliverables;
        public Integer progressPercentage;
        public String createdBy;
        public String createdByName;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public List<ProjectParticipant> participants;
        public List<ProjectMilestone> milestones;
        public int participantCount;
        public int milestoneCount;
    }

    // 项目参与人员
    public static class ProjectParticipant {
        public String id;
        public String projectId;
        public String userId;
        public String userName;
        public String userRole; // 用户系统角色
        public ParticipantRole participantRole; // 项目中的角色
        public LocalDate joinDate;
        public ParticipantStatus status;
        public BigDecimal contributionPercentage;
        public LocalDateTime createdAt;
    }

    // 项目里程碑
    public static class ProjectMilestone {
        public String id;
        public String projectId;
        public String title;
        public String description;
        public LocalDate plannedDate;
        public LocalDate actualDate;
        public MilestoneStatus status;
        public Integer progressPercentage;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }

    // 项目资金记录
    public static class ProjectFund {
        public String id;
        public String projectId;
        public FundType fundType;
        public String category;
        public BigDecimal amount;
        public String currency;
        public String description;
        public LocalDate transactionDate;
        public FundStatus status;
        public String createdBy;
        public String createdByName;
        public LocalDateTime createdAt;
    }

    // 创建项目请求
    public static class CreateProjectRequest {
        @NotBlank
        public String title;
        public String description;
        @NotNull
        public ProjectType projectType;
        @NotNull
        public InitiatorType initiatorType;
        @NotBlank
        public String initiatorId;
        public PartnerType partnerType;
        public String partnerId;
        public ProjectPriority priority = ProjectPriority.MEDIUM;
        @DecimalMin("0")
        public BigDecimal budget;
        public String currency = "CNY";
        public LocalDate startDate;
        public LocalDate endDate;
        public List<String> tags;
        public String requirements;
        public String deliverables;
    }

    // 更新项目请求
    public static class UpdateProjectRequest {
        public String title;
        public String description;
        public ProjectType projectType;
        public PartnerType partnerType;
        public String partnerId;
        public ProjectStatus status;
        public ProjectPriority priority;
        public BigDecimal budget;
        public String currency;
        public LocalDate startDate;
        public LocalDate endDate;
        public LocalDate actualStartDate;
        public LocalDate actualEndDate;
        public List<String> tags;
        public String requirements;
        public String deliverables;
        public Integer progressPercentage;
    }

    // 添加项目参与人员请求
    public static class AddParticipantRequest {
        @NotBlank
        public String userId;
        @NotNull
        public ParticipantRole participantRole;
        public BigDecimal contributionPercentage;
    }

    // 更新项目参与人员请求
    public static class UpdateParticipantRequest {
        public ParticipantRole participantRole;
        public ParticipantStatus status;
        public BigDecimal contributionPercentage;
    }

    // 创建里程碑请求
    public static class CreateMilestoneRequest {
        @NotBlank
        public String title;
        public String description;
        @NotNull
        public LocalDate plannedDate;
    }

    // 更新里程碑请求
    public static class UpdateMilestoneRequest {
        public String title;
        public String description;
        public LocalDate plannedDate;
        public LocalDate actualDate;
        public MilestoneStatus status;
        public Integer progressPercentage;
    }

    // 添加资金记录请求
    public static class AddFundRequest {
        @NotNull
        public FundType fundType;
        public String category;
        @NotNull
        @DecimalMin("0")
        public BigDecimal amount;
        public String currency = "CNY";
        public String description;
        public LocalDate transactionDate;
    }

    // 项目匹配请求
    public static class ProjectMatchRequest {
        @NotBlank
        public String projectId;
        @NotBlank
        public String partnerId;
        public String message; // 合作意向说明
    }

    // 分页结果
    public static class PageResult<T> {
        public List<T> items;
        public long total;
        public int page;
        public int size;
        public long totalPages;
    }

    // 项目统计信息
    public static class ProjectStats {
        public long totalProjects;
        public long activeProjects;
        public long completedProjects;
        public long myProjects;
        public BigDecimal totalBudget;
        public BigDecimal completedBudget;
        public List<ProjectTypeStats> typeStats;
        public List<ProjectStatusStats> statusStats;
    }

    public static class ProjectTypeStats {
        public ProjectType type;
        public long count;
        public BigDecimal totalBudget;
    }

    public static class ProjectStatusStats {
        public ProjectStatus status;
        public long count;
        public BigDecimal totalBudget;
    }

    // 枚举定义
    public enum ProjectType {
        RESEARCH("科研合作"),
        DEVELOPMENT("技术开发"),
        CONSULTING("咨询服务"),
        TRAINING("人才培训"),
        INTERNSHIP("实习项目");

        private final String displayName;
        
        ProjectType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum InitiatorType {
        UNIVERSITY("高校"),
        COMPANY("企业");

        private final String displayName;
        
        InitiatorType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PartnerType {
        UNIVERSITY("高校"),
        COMPANY("企业");

        private final String displayName;
        
        PartnerType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ProjectStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布"),
        MATCHING("匹配中"),
        NEGOTIATING("协商中"),
        APPROVED("已批准"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消");

        private final String displayName;
        
        ProjectStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ProjectPriority {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        URGENT("紧急");

        private final String displayName;
        
        ProjectPriority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ParticipantRole {
        LEADER("负责人"),
        MEMBER("成员"),
        MENTOR("导师"),
        SUPERVISOR("监督员");

        private final String displayName;
        
        ParticipantRole(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ParticipantStatus {
        ACTIVE("活跃"),
        INACTIVE("非活跃"),
        LEFT("已退出");

        private final String displayName;
        
        ParticipantStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum MilestoneStatus {
        PENDING("待开始"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        DELAYED("已延期");

        private final String displayName;
        
        MilestoneStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum FundType {
        BUDGET("预算"),
        EXPENSE("支出"),
        INCOME("收入");

        private final String displayName;
        
        FundType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum FundStatus {
        PLANNED("计划中"),
        APPROVED("已批准"),
        PAID("已支付"),
        CANCELLED("已取消");

        private final String displayName;
        
        FundStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
