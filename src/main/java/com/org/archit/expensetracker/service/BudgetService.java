package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.BudgetDTO;
import com.org.archit.expensetracker.model.Category;

import java.util.List;
import java.util.Optional;

public interface BudgetService {
    BudgetDTO.Response setBudget(BudgetDTO.Request request, Long userId);
    List<BudgetDTO.Response> getBudgetsByMonthAndYear(Long userId, int month, int year);
    BudgetDTO.Response getBudgetById(Long id, Long userId);
    Optional<BudgetDTO.Response> getBudgetByCategoryAndMonth(Long userId, Category category, int month, int year);
    BudgetDTO.Response updateBudget(Long id, BudgetDTO.Request request, Long userId);
    void deleteBudget(Long id, Long userId);
}
