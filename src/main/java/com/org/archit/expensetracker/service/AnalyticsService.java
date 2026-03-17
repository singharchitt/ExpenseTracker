package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.AnalyticsDTO;
import com.org.archit.expensetracker.model.Budget;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.repository.BudgetRepository;
import com.org.archit.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    // ── Full Dashboard Summary ───────────────────────────────────────────────
    public AnalyticsDTO.DashboardSummary getDashboardSummary(int month, int year) {
        LocalDate now = LocalDate.now();

        // This month totals
        BigDecimal totalThisMonth = sumList(expenseRepository.getTotalByCategoryForMonth(month, year));
        int countThisMonth = expenseRepository.findByMonthAndYear(month, year).size();

        // Last month totals
        LocalDate lastMonthDate = LocalDate.of(year, month, 1).minusMonths(1);
        BigDecimal totalLastMonth = sumList(expenseRepository.getTotalByCategoryForMonth(
                lastMonthDate.getMonthValue(), lastMonthDate.getYear()));

        // This year totals
        BigDecimal totalThisYear = expenseRepository.findByDateBetweenOrderByDateDesc(
                        LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
                .stream().map(e -> e.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

        return AnalyticsDTO.DashboardSummary.builder()
                .totalThisMonth(totalThisMonth)
                .totalLastMonth(totalLastMonth)
                .totalThisYear(totalThisYear)
                .totalExpensesThisMonth(countThisMonth)
                .categoryBreakdown(getCategoryBreakdown(month, year))
                .monthlyTotals(getMonthlyTotals(6))
                .budgetComparisons(getBudgetComparisons(month, year))
                .build();
    }

    // ── Category Breakdown (pie chart) ──────────────────────────────────────
    public List<AnalyticsDTO.CategorySummary> getCategoryBreakdown(int month, int year) {
        List<Object[]> raw = expenseRepository.getTotalByCategoryForMonth(month, year);
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

    // ── Monthly Totals (line/bar chart) ─────────────────────────────────────
    public List<AnalyticsDTO.MonthlyTotal> getMonthlyTotals(int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
        List<Object[]> raw = expenseRepository.getMonthlyTotals(startDate);

        return raw.stream().map(r -> {
            int y = ((Number) r[0]).intValue();
            int m = ((Number) r[1]).intValue();
            BigDecimal total = (BigDecimal) r[2];
            String label = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + y;
            return AnalyticsDTO.MonthlyTotal.builder()
                    .year(y).month(m).monthLabel(label).total(total).build();
        }).collect(Collectors.toList());
    }

    // ── Budget vs Actual ─────────────────────────────────────────────────────
    public List<AnalyticsDTO.BudgetComparison> getBudgetComparisons(int month, int year) {
        List<Budget> budgets = budgetRepository.findByMonthAndYear(month, year);

        return budgets.stream().map(budget -> {
            BigDecimal spent = expenseRepository.getTotalForCategoryAndMonth(
                    budget.getCategory(), month, year);
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

    // ── Helper ───────────────────────────────────────────────────────────────
    private BigDecimal sumList(List<Object[]> rows) {
        return rows.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}