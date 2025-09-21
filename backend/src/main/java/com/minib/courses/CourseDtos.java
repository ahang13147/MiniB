package com.minib.courses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class CourseDtos {
    public static class Course {
        public String id;
        public String code;
        public String name;
        public String description;
        public String teacherId;
        public String teacherName;
        public String department;
        public String semester;
        public int credits;
        public int hours;
        public String status; // DRAFT, PUBLISHED, ARCHIVED
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public List<String> students;
        public int studentCount;
    }

    public static class CreateCourseRequest {
        @NotBlank public String code;
        @NotBlank public String name;
        public String description;
        @NotBlank public String teacherId;
        public String department;
        public String semester;
        @NotNull public Integer credits;
        @NotNull public Integer hours;
    }

    public static class UpdateCourseRequest {
        public String name;
        public String description;
        public String teacherId;
        public String department;
        public String semester;
        public Integer credits;
        public Integer hours;
        public String status;
    }

    public static class PageResult<T> {
        public List<T> items;
        public long total;
    }

    public static class AssignStudentRequest {
        @NotBlank public String studentId;
    }

    public static class RemoveStudentRequest {
        @NotBlank public String studentId;
    }
}

