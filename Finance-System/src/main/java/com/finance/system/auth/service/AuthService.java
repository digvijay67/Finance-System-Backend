package com.finance.system.auth.service;

import com.finance.system.auth.dto.AuthDtos.*;
import com.finance.system.entity.User;
import com.finance.system.exception.ApiException;
import com.finance.system.repository.UserRepository;
import com.finance.system.security.jwt.JwtUtils;
import com.finance.system.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    // ── Register ──────────────────────────────────────────────────────────

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already registered: " + request.getEmail(),
                    HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} ({})", user.getEmail(), user.getRole());

        return mapToUserResponse(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

            if (userDetails.getStatus() == User.UserStatus.INACTIVE) {
                throw new ApiException("Account is inactive. Please contact an administrator.",
                        HttpStatus.FORBIDDEN);
            }

            String token = jwtUtils.generateAccessToken(
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getRole().name()
            );

            log.info("User logged in: {}", userDetails.getEmail());

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpirationMs / 1000)
                    .userId(userDetails.getId())
                    .email(userDetails.getEmail())
                    .fullName(userDetails.getFullName())
                    .role(userDetails.getRole().name())
                    .build();

        } catch (DisabledException e) {
            throw new ApiException("Account is disabled.", HttpStatus.FORBIDDEN);
        } catch (BadCredentialsException e) {
            throw new ApiException("Invalid email or password.", HttpStatus.UNAUTHORIZED);
        }
    }

    // ── User Response Mapper ──────────────────────────────────────────────

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
