package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.ExpenseDTO;
import com.org.archit.expensetracker.exception.ResourceNotFoundException;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.model.Expense;
import com.org.archit.expensetracker.model.User;
import com.org.archit.expensetracker.repository.ExpenseRepository;
import com.org.archit.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ExpenseDTO.Response createExpense(ExpenseDTO.Request request, Long userId) {
        User user = findUserOrThrow(userId);
        Expense expense = Expense.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .category(request.getCategory())
                .date(request.getDate())
                .build();
        return toResponse(expenseRepository.save(expense));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseDTO.Response> getAllExpenses(Long userId, Pageable pageable) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO.Response getExpenseById(Long id, Long userId) {
        return toResponse(findOrThrow(id, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseDTO.Response> getExpensesByCategory(Long userId, Category category, Pageable pageable) {
        return expenseRepository.findByUserIdAndCategoryOrderByDateDesc(userId, category, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDTO.Response> getExpensesByMonthAndYear(Long userId, int month, int year) {
        return expenseRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseDTO.Response> getExpensesByDateRange(Long userId, LocalDate start, LocalDate end, Pageable pageable) {
        return expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public ExpenseDTO.Response updateExpense(Long id, ExpenseDTO.Request request, Long userId) {
        Expense expense = findOrThrow(id, userId);
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        return toResponse(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public void deleteExpense(Long id, Long userId) {
        findOrThrow(id, userId);
        expenseRepository.deleteById(id);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Expense findOrThrow(Long id, Long userId) {
        return expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private ExpenseDTO.Response toResponse(Expense expense) {
        return ExpenseDTO.Response.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
