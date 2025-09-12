package com.minib.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

import static com.minib.resources.ResourceEnums.*;

public class ResourceDtos {
    public static class ResearchResource {
        public String id;
        public ResearchResourceType type;
        public String name;
        public String description;
        public Ownership ownership; // ENTERPRISE or SCHOOL
        public BorrowStatus status; // AVAILABLE or BORROWED
        public String publisher; // 发布人
        public String borrower; // 借用人（可空）
        public String location; // 仅对实验设备有意义
    }

    public static class CourseResource {
        public String id;
        public CourseResourceType type;
        public String name;
        public String description;
        public Ownership ownership;
        public String publisher;
    }

    public static class BorrowRequest {
        @NotBlank public String id;
        @NotBlank public String borrower;
    }

    public static class ReturnRequest {
        @NotBlank public String id;
    }

    public static class PageResult<T> {
        public List<T> items;
        public long total;
    }
}


