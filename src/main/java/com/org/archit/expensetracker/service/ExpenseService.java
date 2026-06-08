package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.ExpenseDTO;
import com.org.archit.expensetracker.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpenseDTO.Response createExpense(ExpenseDTO.Request request, Long userId);
    Page<ExpenseDTO.Response> getAllExpenses(Long userId, Pageable pageable);
    ExpenseDTO.Response getExpenseById(Long id, Long userId);
    Page<ExpenseDTO.Response> getExpensesByCategory(Long userId, Category category, Pageable pageable);
    List<ExpenseDTO.Response> getExpensesByMonthAndYear(Long userId, int month, int year);
    Page<ExpenseDTO.Response> getExpensesByDateRange(Long userId, LocalDate start, LocalDate end, Pageable pageable);
    ExpenseDTO.Response updateExpense(Long id, ExpenseDTO.Request request, Long userId);
    void deleteExpense(Long id, Long userId);
}
