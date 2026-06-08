package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.BudgetDTO;
import com.org.archit.expensetracker.exception.ResourceNotFoundException;
import com.org.archit.expensetracker.model.Budget;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.model.User;
import com.org.archit.expensetracker.repository.BudgetRepository;
import com.org.archit.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BudgetDTO.Response setBudget(BudgetDTO.Request request, Long userId) {
        User user = findUserOrThrow(userId);

        Optional<Budget> existing = budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                userId, request.getCategory(), request.getMonth(), request.getYear());

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setBudgetLimit(request.getBudgetLimit());
        } else {
            budget = Budget.builder()
                    .user(user)
                    .category(request.getCategory())
                    .budgetLimit(request.getBudgetLimit())
                    .month(request.getMonth())
                    .year(request.getYear())
                    .build();
        }
        return toResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetDTO.Response> getBudgetsByMonthAndYear(Long userId, int month, int year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetDTO.Response getBudgetById(Long id, Long userId) {
        return toResponse(findOrThrow(id, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BudgetDTO.Response> getBudgetByCategoryAndMonth(Long userId, Category category, int month, int year) {
        return budgetRepository.findByUserIdAndCategoryAndMonthAndYear(userId, category, month, year)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public BudgetDTO.Response updateBudget(Long id, BudgetDTO.Request request, Long userId) {
        Budget budget = findOrThrow(id, userId);
        budget.setCategory(request.getCategory());
        budget.setBudgetLimit(request.getBudgetLimit());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        return toResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public void deleteBudget(Long id, Long userId) {
        findOrThrow(id, userId);
        budgetRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Budget findOrThrow(Long id, Long userId) {
        return budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private BudgetDTO.Response toResponse(Budget budget) {
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
