package com.finance.system.auth.dto;

import com.finance.system.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public final class AuthDtos {

    private AuthDtos() {}

    // ── Register Request ──────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "User registration request")
    public static class RegisterRequest {

        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        @Schema(example = "Alice Johnson")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Schema(example = "alice@finance.com")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).+$",
                 message = "Password must contain at least one uppercase letter and one number")
        @Schema(example = "Password1!")
        private String password;

        @NotNull(message = "Role is required")
        @Schema(example = "ANALYST")
        private User.Role role;
    }

    // ── Login Request ─────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Login request")
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Schema(example = "hii@finance.com")
        private String email;

        @NotBlank(message = "Password is required")
        @Schema(example = "Password1!")
        private String password;
    }

    // ── Auth Response ─────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "JWT token response")
    public static class AuthResponse {

        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
        private String accessToken;

        @Schema(example = "Bearer")
        private String tokenType = "Bearer";

        private Long expiresIn;
        private Long userId;

        @Schema(example = "hii@finance.com")
        private String email;

        @Schema(example = "")
        private String fullName;

        @Schema(example = "ANALYST")
        private String role;
    }

    // ── User Response ─────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "User details")
    public static class UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private String role;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // ── Update User Status Request ────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Update user status")
    public static class UpdateStatusRequest {
        @NotNull(message = "Status is required")
        private User.UserStatus status;
    }
}
