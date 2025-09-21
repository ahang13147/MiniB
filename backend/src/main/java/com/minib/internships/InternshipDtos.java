package com.minib.internships;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InternshipDtos {
    
    // 实习职位信息
    public static class InternshipPosition {
        public String id;
        public String companyId;
        public String companyName;
        public String title;
        public String description;
        public String department;
        public String location;
        public PositionType positionType;
        public Integer durationMonths;
        public BigDecimal salaryMin;
        public BigDecimal salaryMax;
        public String currency;
        public String requirements;
        public String responsibilities;
        public List<String> skillsRequired;
        public List<String> majorPreferred;
        public GradeRequirement gradeRequirement;
        public Integer positionsAvailable;
        public Integer positionsFilled;
        public LocalDate applicationDeadline;
        public LocalDate startDate;
        public LocalDate endDate;
        public PositionStatus status;
        public String createdBy;
        public String createdByName;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public int applicationCount;
        public List<InternshipApplication> applications;
    }

    // 实习申请信息
    public static class InternshipApplication {
        public String id;
        public String positionId;
        public String positionTitle;
        public String companyName;
        public String studentId;
        public String studentName;
        public String studentEmail;
        public String studentPhone;
        public String studentMajor;
        public String studentGrade;
        public String coverLetter;
        public String resumeUrl;
        public String portfolioUrl;
        public BigDecimal gpa;
        public List<String> skills;
        public String experience;
        public ApplicationStatus status;
        public LocalDateTime interviewDate;
        public String interviewFeedback;
        public String rejectionReason;
        public LocalDateTime appliedAt;
        public LocalDateTime updatedAt;
    }

    // 实习记录信息
    public static class InternshipRecord {
        public String id;
        public String applicationId;
        public String studentId;
        public String studentName;
        public String companyId;
        public String companyName;
        public String positionTitle;
        public String mentorId;
        public String mentorName;
        public String supervisorId;
        public String supervisorName;
        public LocalDate startDate;
        public LocalDate endDate;
        public LocalDate actualEndDate;
        public String department;
        public String location;
        public BigDecimal salary;
        public RecordStatus status;
        public BigDecimal performanceScore;
        public String mentorFeedback;
        public String supervisorFeedback;
        public String studentFeedback;
        public String certificateUrl;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }

    // 创建实习职位请求
    public static class CreatePositionRequest {
        @NotBlank
        public String title;
        public String description;
        public String department;
        @NotBlank
        public String location;
        @NotNull
        public PositionType positionType;
        public Integer durationMonths;
        @DecimalMin("0")
        public BigDecimal salaryMin;
        @DecimalMin("0")
        public BigDecimal salaryMax;
        public String currency = "CNY";
        public String requirements;
        public String responsibilities;
        public List<String> skillsRequired;
        public List<String> majorPreferred;
        public GradeRequirement gradeRequirement = GradeRequirement.ANY;
        @Min(1)
        public Integer positionsAvailable = 1;
        @NotNull
        public LocalDate applicationDeadline;
        @NotNull
        public LocalDate startDate;
        public LocalDate endDate;
    }

    // 更新实习职位请求
    public static class UpdatePositionRequest {
        public String title;
        public String description;
        public String department;
        public String location;
        public PositionType positionType;
        public Integer durationMonths;
        public BigDecimal salaryMin;
        public BigDecimal salaryMax;
        public String currency;
        public String requirements;
        public String responsibilities;
        public List<String> skillsRequired;
        public List<String> majorPreferred;
        public GradeRequirement gradeRequirement;
        public Integer positionsAvailable;
        public LocalDate applicationDeadline;
        public LocalDate startDate;
        public LocalDate endDate;
        public PositionStatus status;
    }

    // 申请实习请求
    public static class ApplyInternshipRequest {
        public String coverLetter;
        public String resumeUrl;
        public String portfolioUrl;
        public BigDecimal gpa;
        public List<String> skills;
        public String experience;
    }

    // 更新申请状态请求
    public static class UpdateApplicationRequest {
        @NotNull
        public ApplicationStatus status;
        public LocalDateTime interviewDate;
        public String interviewFeedback;
        public String rejectionReason;
    }

    // 创建实习记录请求
    public static class CreateRecordRequest {
        @NotBlank
        public String applicationId;
        public String mentorId;
        public String supervisorId;
        @NotNull
        public LocalDate startDate;
        public LocalDate endDate;
        public String department;
        public String location;
        public BigDecimal salary;
    }

    // 更新实习记录请求
    public static class UpdateRecordRequest {
        public LocalDate actualEndDate;
        public RecordStatus status;
        public BigDecimal performanceScore;
        public String mentorFeedback;
        public String supervisorFeedback;
        public String studentFeedback;
        public String certificateUrl;
    }

    // 实习统计信息
    public static class InternshipStats {
        public long totalPositions;
        public long activePositions;
        public long totalApplications;
        public long acceptedApplications;
        public long ongoingInternships;
        public long completedInternships;
        public BigDecimal averageSalary;
        public List<CompanyStats> topCompanies;
        public List<MajorStats> topMajors;
        public List<PositionTypeStats> typeStats;
    }

    public static class CompanyStats {
        public String companyId;
        public String companyName;
        public long positionCount;
        public long applicationCount;
    }

    public static class MajorStats {
        public String major;
        public long applicationCount;
        public long acceptedCount;
    }

    public static class PositionTypeStats {
        public PositionType type;
        public long count;
        public BigDecimal averageSalary;
    }

    // 分页结果
    public static class PageResult<T> {
        public List<T> items;
        public long total;
        public int page;
        public int size;
        public long totalPages;
    }

    // 枚举定义
    public enum PositionType {
        FULL_TIME("全职"),
        PART_TIME("兼职"),
        REMOTE("远程");

        private final String displayName;
        
        PositionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum GradeRequirement {
        FRESHMAN("大一"),
        SOPHOMORE("大二"),
        JUNIOR("大三"),
        SENIOR("大四"),
        GRADUATE("研究生"),
        ANY("不限");

        private final String displayName;
        
        GradeRequirement(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PositionStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布"),
        CLOSED("已关闭"),
        CANCELLED("已取消");

        private final String displayName;
        
        PositionStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ApplicationStatus {
        SUBMITTED("已提交"),
        UNDER_REVIEW("审核中"),
        INTERVIEW_SCHEDULED("面试安排"),
        INTERVIEWED("已面试"),
        ACCEPTED("已录取"),
        REJECTED("已拒绝"),
        WITHDRAWN("已撤回");

        private final String displayName;
        
        ApplicationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RecordStatus {
        ONGOING("进行中"),
        COMPLETED("已完成"),
        TERMINATED("已终止"),
        EXTENDED("已延期");

        private final String displayName;
        
        RecordStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
