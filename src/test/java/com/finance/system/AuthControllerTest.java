package com.finance.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.system.auth.dto.AuthDtos.*;
import com.finance.system.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Register: valid request → 201")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .fullName("Test User")
                .email("testuser@finance.com")
                .password("Password1!")
                .role(User.Role.VIEWER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("testuser@finance.com"));
    }

    @Test
    @DisplayName("Register: weak password → 400")
    void register_weakPassword_returns400() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .fullName("Test User")
                .email("weak@finance.com")
                .password("weak")
                .role(User.Role.VIEWER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Login: wrong password → 401")
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("admin@finance.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
