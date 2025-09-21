package com.minib.achievements;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AchievementDtos {
    
    // 学生成果信息
    public static class StudentAchievement {
        public String id;
        public String studentId;
        public String studentName;
        public String studentMajor;
        public String studentGrade;
        public String title;
        public String description;
        public AchievementType achievementType;
        public String category;
        public AchievementLevel level;
        public String awardRank;
        public String organization;
        public LocalDate achievementDate;
        public String certificateUrl;
        public List<String> attachmentUrls;
        public Visibility visibility;
        public VerificationStatus status;
        public String verificationNotes;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }

    // 创建成果请求
    public static class CreateAchievementRequest {
        @NotBlank
        public String title;
        public String description;
        @NotNull
        public AchievementType achievementType;
        public String category;
        @NotNull
        public AchievementLevel level;
        public String awardRank;
        public String organization;
        @NotNull
        public LocalDate achievementDate;
        public String certificateUrl;
        public List<String> attachmentUrls;
        public Visibility visibility = Visibility.UNIVERSITY;
    }

    // 更新成果请求
    public static class UpdateAchievementRequest {
        public String title;
        public String description;
        public AchievementType achievementType;
        public String category;
        public AchievementLevel level;
        public String awardRank;
        public String organization;
        public LocalDate achievementDate;
        public String certificateUrl;
        public List<String> attachmentUrls;
        public Visibility visibility;
    }

    // 验证成果请求
    public static class VerifyAchievementRequest {
        @NotNull
        public VerificationStatus status;
        public String verificationNotes;
    }

    // 成果统计信息
    public static class AchievementStats {
        public long totalAchievements;
        public long verifiedAchievements;
        public long pendingVerification;
        public List<TypeStats> typeStats;
        public List<LevelStats> levelStats;
        public List<StudentStats> topStudents;
        public List<CategoryStats> categoryStats;
    }

    public static class TypeStats {
        public AchievementType type;
        public long count;
        public long verifiedCount;
    }

    public static class LevelStats {
        public AchievementLevel level;
        public long count;
        public long verifiedCount;
    }

    public static class StudentStats {
        public String studentId;
        public String studentName;
        public String studentMajor;
        public long achievementCount;
        public long verifiedCount;
    }

    public static class CategoryStats {
        public String category;
        public long count;
        public long verifiedCount;
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
    public enum AchievementType {
        COMPETITION("竞赛获奖"),
        RESEARCH("科研成果"),
        PROJECT("项目作品"),
        PATENT("专利发明"),
        PUBLICATION("论文发表"),
        CERTIFICATE("资格证书"),
        OTHER("其他成果");

        private final String displayName;
        
        AchievementType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AchievementLevel {
        SCHOOL("校级"),
        CITY("市级"),
        PROVINCE("省级"),
        NATIONAL("国家级"),
        INTERNATIONAL("国际级");

        private final String displayName;
        
        AchievementLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Visibility {
        PRIVATE("私有"),
        UNIVERSITY("校内可见"),
        PUBLIC("公开");

        private final String displayName;
        
        Visibility(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum VerificationStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布"),
        VERIFIED("已验证"),
        REJECTED("已拒绝");

        private final String displayName;
        
        VerificationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
