package com.minib.companies;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class CompanyDtos {
    public static class Company {
        public String id;
        public String name;
        public String description;
        public String industry;
        public String scale; // SMALL, MEDIUM, LARGE
        public String website;
        @Email public String email;
        public String phone;
        public String address;
        public String contactPerson;
        public String contactTitle;
        public String status; // PENDING, APPROVED, REJECTED, SUSPENDED
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public List<String> mentors;
        public int mentorCount;
        public List<String> projects;
        public int projectCount;
    }

    public static class CreateCompanyRequest {
        @NotBlank public String name;
        public String description;
        public String industry;
        public String scale;
        public String website;
        @Email public String email;
        public String phone;
        public String address;
        public String contactPerson;
        public String contactTitle;
    }

    public static class UpdateCompanyRequest {
        public String name;
        public String description;
        public String industry;
        public String scale;
        public String website;
        @Email public String email;
        public String phone;
        public String address;
        public String contactPerson;
        public String contactTitle;
        public String status;
    }

    public static class PageResult<T> {
        public List<T> items;
        public long total;
    }

    public static class AssignMentorRequest {
        @NotBlank public String mentorId;
    }

    public static class RemoveMentorRequest {
        @NotBlank public String mentorId;
    }
}

