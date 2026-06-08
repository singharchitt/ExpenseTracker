package com.org.archit.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.archit.expensetracker.dto.AuthDTO;
import com.org.archit.expensetracker.exception.DuplicateResourceException;
import com.org.archit.expensetracker.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.org.archit.expensetracker.security.JwtUtils jwtUtils;
    @MockBean
    private com.org.archit.expensetracker.security.JwtAuthFilter jwtAuthFilter;
    @MockBean
    private com.org.archit.expensetracker.repository.UserRepository userRepository;

    @Test
    @DisplayName("POST /api/auth/register - should return 201 with token on success")
    void register_validRequest_returns201() throws Exception {
        AuthDTO.RegisterRequest request = AuthDTO.RegisterRequest.builder()
                .name("Archit Singh")
                .email("archit@test.com")
                .password("password123")
                .build();

        AuthDTO.AuthResponse response = AuthDTO.AuthResponse.builder()
                .token("jwt.token.here")
                .email("archit@test.com")
                .name("Archit Singh")
                .tokenType("Bearer")
                .build();

        when(authService.register(any(AuthDTO.RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 409 when email already exists")
    void register_duplicateEmail_returns409() throws Exception {
        AuthDTO.RegisterRequest request = AuthDTO.RegisterRequest.builder()
                .name("Archit Singh")
                .email("archit@test.com")
                .password("password123")
                .build();

        when(authService.register(any(AuthDTO.RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("Email already registered: archit@test.com"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 when password is too short")
    void register_shortPassword_returns400() throws Exception {
        AuthDTO.RegisterRequest request = AuthDTO.RegisterRequest.builder()
                .name("Archit Singh")
                .email("archit@test.com")
                .password("123")  // too short
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 200 with token on valid credentials")
    void login_validCredentials_returns200() throws Exception {
        AuthDTO.LoginRequest request = AuthDTO.LoginRequest.builder()
                .email("archit@test.com")
                .password("password123")
                .build();

        AuthDTO.AuthResponse response = AuthDTO.AuthResponse.builder()
                .token("jwt.token.here")
                .email("archit@test.com")
                .name("Archit Singh")
                .tokenType("Bearer")
                .build();

        when(authService.login(any(AuthDTO.LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("archit@test.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 400 when email is invalid format")
    void login_invalidEmail_returns400() throws Exception {
        AuthDTO.LoginRequest request = AuthDTO.LoginRequest.builder()
                .email("not-an-email")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
