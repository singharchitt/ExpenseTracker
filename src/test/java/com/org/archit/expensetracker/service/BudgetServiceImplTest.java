package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.BudgetDTO;
import com.org.archit.expensetracker.exception.ResourceNotFoundException;
import com.org.archit.expensetracker.model.Budget;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.model.User;
import com.org.archit.expensetracker.repository.BudgetRepository;
import com.org.archit.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private User testUser;
    private Budget testBudget;
    private BudgetDTO.Request testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Archit Singh")
                .email("archit@test.com")
                .password("encodedPassword")
                .build();

        testBudget = Budget.builder()
                .id(1L)
                .user(testUser)
                .category(Category.FOOD)
                .budgetLimit(new BigDecimal("5000.00"))
                .month(6)
                .year(2026)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = BudgetDTO.Request.builder()
                .category(Category.FOOD)
                .budgetLimit(new BigDecimal("5000.00"))
                .month(6)
                .year(2026)
                .build();
    }

    // ── Set Budget ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setBudget: should create new budget when none exists")
    void setBudget_newBudget_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserIdAndCategoryAndMonthAndYear(1L, Category.FOOD, 6, 2026))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        BudgetDTO.Response response = budgetService.setBudget(testRequest, 1L);

        assertThat(response.getCategory()).isEqualTo(Category.FOOD);
        assertThat(response.getBudgetLimit()).isEqualByComparingTo(new BigDecimal("5000.00"));
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("setBudget: should update existing budget instead of creating duplicate")
    void setBudget_existingBudget_updatesLimit() {
        BudgetDTO.Request updateRequest = BudgetDTO.Request.builder()
                .category(Category.FOOD)
                .budgetLimit(new BigDecimal("8000.00"))
                .month(6)
                .year(2026)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserIdAndCategoryAndMonthAndYear(1L, Category.FOOD, 6, 2026))
                .thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        budgetService.setBudget(updateRequest, 1L);

        verify(budgetRepository, times(1)).save(any(Budget.class));
        // No new budget created, existing one updated
        assertThat(testBudget.getBudgetLimit()).isEqualByComparingTo(new BigDecimal("8000.00"));
    }

    // ── Get Budgets ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getBudgetsByMonthAndYear: should return budgets for user's month/year")
    void getBudgetsByMonthAndYear_success() {
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 6, 2026))
                .thenReturn(List.of(testBudget));

        List<BudgetDTO.Response> result = budgetService.getBudgetsByMonthAndYear(1L, 6, 2026);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMonth()).isEqualTo(6);
    }

    @Test
    @DisplayName("getBudgetById: should return budget when it belongs to user")
    void getBudgetById_success() {
        when(budgetRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBudget));

        BudgetDTO.Response response = budgetService.getBudgetById(1L, 1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getBudgetById: should throw when budget doesn't belong to user")
    void getBudgetById_notFound_throwsException() {
        when(budgetRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getBudgetById(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget");
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteBudget: should delete when budget belongs to user")
    void deleteBudget_success() {
        when(budgetRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBudget));

        budgetService.deleteBudget(1L, 1L);

        verify(budgetRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteBudget: should throw and NOT delete when budget not found for user")
    void deleteBudget_notFound_throwsAndDoesNotDelete() {
        when(budgetRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.deleteBudget(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(budgetRepository, never()).deleteById(any());
    }
}
