package com.minib.users;

import com.minib.auth.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class UserDtos {
    public static class User {
        public String id;
        public String username;
        public String displayName;
        public String email;
        public String phone;
        public Role role;
        public String department;
        public String position;
        public boolean enabled;
        public LocalDateTime createdAt;
        public LocalDateTime lastLoginAt;
    }

    public static class CreateUserRequest {
        @NotBlank public String username;
        @NotBlank public String password;
        public String displayName;
        @Email public String email;
        public String phone;
        @NotNull public Role role;
        public String department;
        public String position;
    }

    public static class UpdateUserRequest {
        public String displayName;
        @Email public String email;
        public String phone;
        public Role role;
        public String department;
        public String position;
        public Boolean enabled;
    }

    public static class PageResult<T> {
        public List<T> items;
        public long total;
    }
}

