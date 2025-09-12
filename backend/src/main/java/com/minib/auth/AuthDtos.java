package com.minib.auth;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AuthDtos {
    public static class LoginRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
    }

    public static class LoginResponse {
        public String token;
        public UserProfile user;
    }

    public static class UserProfile {
        public String id;
        public String displayName;
        public Role role;
        public List<Permission> permissions;
    }

    public static class RegisterRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
        public String displayName;
        public Role role;
    }

    public static class ForgotRequest {
        @NotBlank
        public String username;
    }

    public static class ResetRequest {
        @NotBlank
        public String token;
        @NotBlank
        public String newPassword;
    }
}




