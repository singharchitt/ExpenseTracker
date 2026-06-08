package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.AnalyticsDTO;
import com.org.archit.expensetracker.model.Budget;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.repository.BudgetRepository;
import com.org.archit.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsDTO.DashboardSummary getDashboardSummary(Long userId, int month, int year) {
        // This month total — uses SUM query, not loading all objects
        BigDecimal totalThisMonth = sumCategoryTotals(
                expenseRepository.getTotalByCategoryForMonth(userId, month, year));

        // Count — uses COUNT query, not .size() on a full list
        int countThisMonth = expenseRepository.countByUserIdAndMonthAndYear(userId, month, year);

        // Last month total
        LocalDate lastMonthDate = LocalDate.of(year, month, 1).minusMonths(1);
        BigDecimal totalLastMonth = sumCategoryTotals(
                expenseRepository.getTotalByCategoryForMonth(
                        userId, lastMonthDate.getMonthValue(), lastMonthDate.getYear()));

        // Year total — single SUM query
        BigDecimal totalThisYear = expenseRepository.getTotalForYear(userId, year);

        return AnalyticsDTO.DashboardSummary.builder()
                .totalThisMonth(totalThisMonth)
                .totalLastMonth(totalLastMonth)
                .totalThisYear(totalThisYear)
                .totalExpensesThisMonth(countThisMonth)
                .categoryBreakdown(getCategoryBreakdown(userId, month, year))
                .monthlyTotals(getMonthlyTotals(userId, 6))
                .budgetComparisons(getBudgetComparisons(userId, month, year))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalyticsDTO.CategorySummary> getCategoryBreakdown(Long userId, int month, int year) {
        List<Object[]> raw = expenseRepository.getTotalByCategoryForMonth(userId, month, year);
        BigDecimal grandTotal = raw.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return raw.stream().map(r -> {
            Category cat = (Category) r[0];
            BigDecimal total = (BigDecimal) r[1];
            double pct = grandTotal.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                    total.divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
            return AnalyticsDTO.CategorySummary.builder()
                    .category(cat).total(total).percentage(pct).build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalyticsDTO.MonthlyTotal> getMonthlyTotals(Long userId, int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
        List<Object[]> raw = expenseRepository.getMonthlyTotals(userId, startDate);

        return raw.stream().map(r -> {
            int y = ((Number) r[0]).intValue();
            int m = ((Number) r[1]).intValue();
            BigDecimal total = (BigDecimal) r[2];
            String label = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + y;
            return AnalyticsDTO.MonthlyTotal.builder()
                    .year(y).month(m).monthLabel(label).total(total).build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalyticsDTO.BudgetComparison> getBudgetComparisons(Long userId, int month, int year) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

        // Fix N+1: fetch ALL category totals in one query, then map in memory
        Map<Category, BigDecimal> spentByCategory = expenseRepository
                .getAllCategoryTotalsForMonth(userId, month, year)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Category) r[0],
                        r -> (BigDecimal) r[1]
                ));

        return budgets.stream().map(budget -> {
            BigDecimal spent = spentByCategory.getOrDefault(budget.getCategory(), BigDecimal.ZERO);
            BigDecimal limit = budget.getBudgetLimit();
            BigDecimal remaining = limit.subtract(spent);
            double pct = limit.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                    spent.divide(limit, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();

            return AnalyticsDTO.BudgetComparison.builder()
                    .category(budget.getCategory())
                    .budgetLimit(limit)
                    .actualSpent(spent)
                    .remaining(remaining)
                    .percentageUsed(Math.min(pct, 100))
                    .isOverBudget(spent.compareTo(limit) > 0)
                    .build();
        }).collect(Collectors.toList());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private BigDecimal sumCategoryTotals(List<Object[]> rows) {
        return rows.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
