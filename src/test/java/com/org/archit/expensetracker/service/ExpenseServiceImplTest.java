package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.ExpenseDTO;
import com.org.archit.expensetracker.exception.ResourceNotFoundException;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.model.Expense;
import com.org.archit.expensetracker.model.User;
import com.org.archit.expensetracker.repository.ExpenseRepository;
import com.org.archit.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private User testUser;
    private Expense testExpense;
    private ExpenseDTO.Request testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Archit Singh")
                .email("archit@test.com")
                .password("encodedPassword")
                .build();

        testExpense = Expense.builder()
                .id(1L)
                .user(testUser)
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

    // ── Create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createExpense: should save and return expense response")
    void createExpense_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        ExpenseDTO.Response response = expenseService.createExpense(testRequest, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Lunch");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(response.getCategory()).isEqualTo(Category.FOOD);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    @DisplayName("createExpense: should throw when user not found")
    void createExpense_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createExpense(testRequest, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getExpenseById: should return expense when it belongs to user")
    void getExpenseById_success() {
        when(expenseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testExpense));

        ExpenseDTO.Response response = expenseService.getExpenseById(1L, 1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Lunch");
    }

    @Test
    @DisplayName("getExpenseById: should throw ResourceNotFoundException when not found")
    void getExpenseById_notFound_throwsException() {
        when(expenseRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense");
    }

    @Test
    @DisplayName("getAllExpenses: should return paginated results for the user")
    void getAllExpenses_success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Expense> page = new PageImpl<>(List.of(testExpense));
        when(expenseRepository.findByUserIdOrderByDateDesc(1L, pageable)).thenReturn(page);

        Page<ExpenseDTO.Response> result = expenseService.getAllExpenses(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Lunch");
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateExpense: should update fields and return response")
    void updateExpense_success() {
        ExpenseDTO.Request updateRequest = ExpenseDTO.Request.builder()
                .title("Dinner")
                .description("Updated")
                .amount(new BigDecimal("600.00"))
                .category(Category.FOOD)
                .date(LocalDate.now())
                .build();

        when(expenseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        ExpenseDTO.Response response = expenseService.updateExpense(1L, updateRequest, 1L);

        assertThat(response).isNotNull();
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    @DisplayName("updateExpense: should throw when expense does not belong to user")
    void updateExpense_wrongUser_throwsException() {
        when(expenseRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(1L, testRequest, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteExpense: should call deleteById when expense belongs to user")
    void deleteExpense_success() {
        when(expenseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testExpense));

        expenseService.deleteExpense(1L, 1L);

        verify(expenseRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteExpense: should throw when expense not found for user")
    void deleteExpense_notFound_throwsException() {
        when(expenseRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(expenseRepository, never()).deleteById(any());
    }

    // ── Month/Year filtering ──────────────────────────────────────────────────

    @Test
    @DisplayName("getExpensesByMonthAndYear: should return list for given month/year")
    void getExpensesByMonthAndYear_success() {
        when(expenseRepository.findByUserIdAndMonthAndYear(1L, 6, 2026))
                .thenReturn(List.of(testExpense));

        List<ExpenseDTO.Response> result = expenseService.getExpensesByMonthAndYear(1L, 6, 2026);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(Category.FOOD);
    }
}
