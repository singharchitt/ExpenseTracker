package com.org.archit.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.archit.expensetracker.dto.ExpenseDTO;
import com.org.archit.expensetracker.exception.ResourceNotFoundException;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.model.User;
import com.org.archit.expensetracker.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    // Security beans needed by WebMvcTest context
    @MockBean
    private com.org.archit.expensetracker.security.JwtUtils jwtUtils;
    @MockBean
    private com.org.archit.expensetracker.security.JwtAuthFilter jwtAuthFilter;
    @MockBean
    private com.org.archit.expensetracker.repository.UserRepository userRepository;

    private User testUser;
    private ExpenseDTO.Response testResponse;
    private ExpenseDTO.Request testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Archit Singh")
                .email("archit@test.com")
                .password("encodedPassword")
                .build();

        testResponse = ExpenseDTO.Response.builder()
                .id(1L)
                .title("Lunch")
                .description("Team lunch")
                .amount(new BigDecimal("450.00"))
                .category(Category.FOOD)
                .date(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = ExpenseDTO.Request.builder()
                .title("Lunch")
                .description("Team lunch")
                .amount(new BigDecimal("450.00"))
                .category(Category.FOOD)
                .date(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/expenses - should return paginated expenses for authenticated user")
    void getExpenses_authenticated_returnsOk() throws Exception {
        when(expenseService.getAllExpenses(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testResponse)));

        mockMvc.perform(get("/api/expenses")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Lunch"))
                .andExpect(jsonPath("$.content[0].category").value("FOOD"));
    }

    @Test
    @DisplayName("GET /api/expenses - should return 401 when not authenticated")
    void getExpenses_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/expenses/{id} - should return expense by id")
    void getExpenseById_success() throws Exception {
        when(expenseService.getExpenseById(1L, 1L)).thenReturn(testResponse);

        mockMvc.perform(get("/api/expenses/1")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Lunch"));
    }

    @Test
    @DisplayName("GET /api/expenses/{id} - should return 404 when not found")
    void getExpenseById_notFound_returns404() throws Exception {
        when(expenseService.getExpenseById(99L, 1L))
                .thenThrow(new ResourceNotFoundException("Expense", 99L));

        mockMvc.perform(get("/api/expenses/99")
                        .with(user(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/expenses - should create expense and return 201")
    void createExpense_validRequest_returns201() throws Exception {
        when(expenseService.createExpense(any(ExpenseDTO.Request.class), eq(1L)))
                .thenReturn(testResponse);

        mockMvc.perform(post("/api/expenses")
                        .with(user(testUser))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Lunch"));
    }

    @Test
    @DisplayName("POST /api/expenses - should return 400 when title is blank")
    void createExpense_blankTitle_returns400() throws Exception {
        ExpenseDTO.Request invalidRequest = ExpenseDTO.Request.builder()
                .title("")
                .amount(new BigDecimal("100.00"))
                .category(Category.FOOD)
                .date(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/expenses")
                        .with(user(testUser))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/expenses/{id} - should return 204 on successful delete")
    void deleteExpense_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/expenses/1")
                        .with(user(testUser))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }
}
