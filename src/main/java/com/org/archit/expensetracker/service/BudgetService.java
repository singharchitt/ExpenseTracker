package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.BudgetDTO;
import com.org.archit.expensetracker.exception.ResourceNotFoundException;
import com.org.archit.expensetracker.model.Budget;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    // ── Create or Update ────────────────────────────────────────────────────
    public BudgetDTO.Response setbudget(BudgetDTO.Request request) {
        // If budget already exists for this category/month/year, update it
        Optional<Budget> existing = budgetRepository.findByCategoryAndMonthAndYear(
                request.getCategory(), request.getMonth(), request.getYear());

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setBudgetLimit(request.getBudgetLimit());
        } else {
            budget = Budget.builder()
                    .category(request.getCategory())
                    .budgetLimit(request.getBudgetLimit())
                    .month(request.getMonth())
                    .year(request.getYear())
                    .build();
        }
        return toResponse(budgetRepository.save(budget));
    }

    // ── Get all budgets for a month/year ────────────────────────────────────
    public List<BudgetDTO.Response> getBudgetsByMonthAndYear(int month, int year) {
        return budgetRepository.findByMonthAndYear(month, year)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Get one budget ───────────────────────────────────────────────────────
    public BudgetDTO.Response getBudgetById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ── Get budget by category/month/year ───────────────────────────────────
    public Optional<BudgetDTO.Response> getBudgetByCategoryAndMonth(
            Category category, int month, int year) {
        return budgetRepository.findByCategoryAndMonthAndYear(category, month, year)
                .map(this::toResponse);
    }

    // ── Update ───────────────────────────────────────────────────────────────
    public BudgetDTO.Response updateBudget(Long id, BudgetDTO.Request request) {
        Budget budget = findOrThrow(id);
        budget.setCategory(request.getCategory());
        budget.setBudgetLimit(request.getBudgetLimit());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        return toResponse(budgetRepository.save(budget));
    }

    // ── Delete ───────────────────────────────────────────────────────────────
    public void deleteBudget(Long id) {
        findOrThrow(id);
        budgetRepository.deleteById(id);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private Budget findOrThrow(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
    }

    public BudgetDTO.Response toResponse(Budget budget) {
        return BudgetDTO.Response.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .budgetLimit(budget.getBudgetLimit())
                .month(budget.getMonth())
                .year(budget.getYear())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}