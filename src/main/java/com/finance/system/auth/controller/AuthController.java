package com.finance.system.auth.controller;

import com.finance.system.auth.dto.AuthDtos.*;
import com.finance.system.auth.service.AuthService;
import com.finance.system.common.ApiResponse;
import com.finance.system.entity.User;
import com.finance.system.exception.ApiException;
import com.finance.system.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Auth & Users", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    // ── Auth Endpoints ────────────────────────────────────────────────────

    @PostMapping("/auth/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Login and receive JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ── User Management Endpoints ─────────────────────────────────────────

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get all users", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(authService::mapToUserResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found with id: " + id, HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success("User retrieved", authService.mapToUserResponse(user)));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status (ADMIN only)", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found with id: " + id, HttpStatus.NOT_FOUND));
        user.setStatus(request.getStatus());
        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User status updated", authService.mapToUserResponse(user)));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (ADMIN only)", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ApiException("User not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }
}
