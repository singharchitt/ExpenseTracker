package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.ExpenseDTO;
import com.org.archit.expensetracker.exception.ResourceNotFoundException;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.model.Expense;
import com.org.archit.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    // ── Create ──────────────────────────────────────────────────────────────
    public ExpenseDTO.Response createExpense(ExpenseDTO.Request request) {
        Expense expense = Expense.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .category(request.getCategory())
                .date(request.getDate())
                .build();
        return toResponse(expenseRepository.save(expense));
    }

    // ── Read All ────────────────────────────────────────────────────────────
    public List<ExpenseDTO.Response> getAllExpenses() {
        return expenseRepository.findAllByOrderByDateDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Read One ────────────────────────────────────────────────────────────
    public ExpenseDTO.Response getExpenseById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ── Read by Category ────────────────────────────────────────────────────
    public List<ExpenseDTO.Response> getExpensesByCategory(Category category) {
        return expenseRepository.findByCategoryOrderByDateDesc(category)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Read by Month/Year ──────────────────────────────────────────────────
    public List<ExpenseDTO.Response> getExpensesByMonthAndYear(int month, int year) {
        return expenseRepository.findByMonthAndYear(month, year)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Read by Date Range ──────────────────────────────────────────────────
    public List<ExpenseDTO.Response> getExpensesByDateRange(LocalDate start, LocalDate end) {
        return expenseRepository.findByDateBetweenOrderByDateDesc(start, end)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Update ──────────────────────────────────────────────────────────────
    public ExpenseDTO.Response updateExpense(Long id, ExpenseDTO.Request request) {
        Expense expense = findOrThrow(id);
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        return toResponse(expenseRepository.save(expense));
    }

    // ── Delete ──────────────────────────────────────────────────────────────
    public void deleteExpense(Long id) {
        findOrThrow(id);
        expenseRepository.deleteById(id);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    private Expense findOrThrow(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    public ExpenseDTO.Response toResponse(Expense expense) {
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