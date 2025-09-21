package com.minib.students;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class StudentDtos {
    public static class Student {
        public String id;
        public String username;
        public String displayName;
        @Email public String email;
        public String phone;
        public String studentId;
        public String department;
        public String major;
        public String grade;
        public String class_;
        public String status; // ENROLLED, GRADUATED, SUSPENDED, DROPPED
        public LocalDateTime enrollmentDate;
        public LocalDateTime createdAt;
        public LocalDateTime lastLoginAt;
        public List<String> courses;
        public int courseCount;
        public List<Grade> grades;
        public double gpa;
        public List<String> projects;
        public int projectCount;
    }

    public static class Grade {
        public String courseId;
        public String courseName;
        public String courseCode;
        public int credits;
        public String score;
        public String grade;
        public String semester;
        public LocalDateTime examDate;
    }

    public static class CreateStudentRequest {
        @NotBlank public String username;
        @NotBlank public String password;
        public String displayName;
        @Email public String email;
        public String phone;
        @NotBlank public String studentId;
        public String department;
        public String major;
        public String grade;
        public String class_;
    }

    public static class UpdateStudentRequest {
        public String displayName;
        @Email public String email;
        public String phone;
        public String department;
        public String major;
        public String grade;
        public String class_;
        public String status;
    }

    public static class PageResult<T> {
        public List<T> items;
        public long total;
    }

    public static class AddGradeRequest {
        @NotBlank public String courseId;
        @NotBlank public String courseName;
        @NotBlank public String courseCode;
        @NotNull public Integer credits;
        @NotBlank public String score;
        @NotBlank public String grade;
        public String semester;
    }

    public static class UpdateGradeRequest {
        public String score;
        public String grade;
    }
}

