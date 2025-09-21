package com.minib.mentors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MentorDtos {
    
    // 双导师课堂信息
    public static class DualMentorCourse {
        public String id;
        public String courseCode;
        public String courseName;
        public String description;
        public String universityId;
        public String universityName;
        public String companyId;
        public String companyName;
        public String universityTeacherId;
        public String universityTeacherName;
        public String enterpriseMentorId;
        public String enterpriseMentorName;
        public String department;
        public String majorId;
        public String majorName;
        public String semester;
        public String academicYear;
        public Integer credits;
        public Integer totalHours;
        public Integer theoryHours;
        public Integer practiceHours;
        public Integer maxStudents;
        public Integer enrolledStudents;
        public CourseType courseType;
        public TeachingMode teachingMode;
        public CourseStatus status;
        public LocalDate startDate;
        public LocalDate endDate;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public List<CourseEnrollment> enrollments;
    }

    // 课程选课信息
    public static class CourseEnrollment {
        public String id;
        public String courseId;
        public String courseName;
        public String studentId;
        public String studentName;
        public String studentMajor;
        public LocalDate enrollmentDate;
        public EnrollmentStatus status;
        public Double finalScore;
        public String finalGrade;
        public Double theoryScore;
        public Double practiceScore;
        public Double attendanceRate;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }

    // 企业导师信息
    public static class EnterpriseMentor {
        public String id;
        public String userId;
        public String name;
        public String email;
        public String phone;
        public String companyId;
        public String companyName;
        public String position;
        public String department;
        public Integer workYears;
        public String expertise;
        public String education;
        public String certifications;
        public String introduction;
        public MentorStatus status;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public List<String> courses;
        public int courseCount;
        public List<String> students;
        public int studentCount;
    }

    // 创建双导师课堂请求
    public static class CreateDualCourseRequest {
        @NotBlank
        public String courseCode;
        @NotBlank
        public String courseName;
        public String description;
        @NotBlank
        public String universityId;
        @NotBlank
        public String companyId;
        @NotBlank
        public String universityTeacherId;
        @NotBlank
        public String enterpriseMentorId;
        public String department;
        public String majorId;
        public String semester;
        public String academicYear;
        @Min(1)
        public Integer credits = 1;
        @Min(1)
        public Integer totalHours = 16;
        public Integer theoryHours = 8;
        public Integer practiceHours = 8;
        @Min(1)
        public Integer maxStudents = 30;
        @NotNull
        public CourseType courseType;
        @NotNull
        public TeachingMode teachingMode;
        public LocalDate startDate;
        public LocalDate endDate;
    }

    // 更新双导师课堂请求
    public static class UpdateDualCourseRequest {
        public String courseName;
        public String description;
        public String universityTeacherId;
        public String enterpriseMentorId;
        public String department;
        public String majorId;
        public String semester;
        public String academicYear;
        public Integer credits;
        public Integer totalHours;
        public Integer theoryHours;
        public Integer practiceHours;
        public Integer maxStudents;
        public CourseType courseType;
        public TeachingMode teachingMode;
        public CourseStatus status;
        public LocalDate startDate;
        public LocalDate endDate;
    }

    // 学生选课请求
    public static class EnrollCourseRequest {
        @NotBlank
        public String courseId;
        @NotBlank
        public String studentId;
    }

    // 更新选课信息请求
    public static class UpdateEnrollmentRequest {
        public EnrollmentStatus status;
        public Double finalScore;
        public String finalGrade;
        public Double theoryScore;
        public Double practiceScore;
        public Double attendanceRate;
    }

    // 企业导师注册请求
    public static class RegisterMentorRequest {
        @NotBlank
        public String userId;
        @NotBlank
        public String companyId;
        @NotBlank
        public String position;
        public String department;
        public Integer workYears;
        public String expertise;
        public String education;
        public String certifications;
        public String introduction;
    }

    // 更新企业导师请求
    public static class UpdateMentorRequest {
        public String position;
        public String department;
        public Integer workYears;
        public String expertise;
        public String education;
        public String certifications;
        public String introduction;
        public MentorStatus status;
    }

    // 导师统计信息
    public static class MentorStats {
        public long totalCourses;
        public long activeCourses;
        public long completedCourses;
        public long totalMentors;
        public long activeMentors;
        public long totalEnrollments;
        public long completedEnrollments;
        public Double averageScore;
        public List<CompanyMentorStats> topCompanies;
        public List<CourseModeStats> modeStats;
    }

    public static class CompanyMentorStats {
        public String companyId;
        public String companyName;
        public long mentorCount;
        public long courseCount;
    }

    public static class CourseModeStats {
        public TeachingMode mode;
        public long count;
        public long enrollmentCount;
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
    public enum CourseType {
        THEORY("理论课程"),
        PRACTICE("实践课程"),
        MIXED("理实结合");

        private final String displayName;
        
        CourseType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TeachingMode {
        ONLINE("线上"),
        OFFLINE("线下"),
        HYBRID("混合");

        private final String displayName;
        
        TeachingMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CourseStatus {
        PLANNING("筹划中"),
        RECRUITING("招生中"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消");

        private final String displayName;
        
        CourseStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum EnrollmentStatus {
        ENROLLED("已选课"),
        COMPLETED("已完成"),
        DROPPED("已退课"),
        FAILED("不及格");

        private final String displayName;
        
        EnrollmentStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum MentorStatus {
        ACTIVE("活跃"),
        INACTIVE("非活跃"),
        SUSPENDED("已暂停");

        private final String displayName;
        
        MentorStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
