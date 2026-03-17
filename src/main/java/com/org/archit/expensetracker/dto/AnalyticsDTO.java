package com.org.archit.expensetracker.dto;

import com.org.archit.expensetracker.model.Category;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AnalyticsDTO {

    // ── Category breakdown (for pie chart) ─────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySummary {
        private Category category;
        private BigDecimal total;
        private double percentage;
    }

    // ── Monthly total (for bar/line chart) ─────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyTotal {
        private int year;
        private int month;
        private String monthLabel; // e.g. "Jan 2025"
        private BigDecimal total;
    }

    // ── Budget vs Actual (for budget progress bars) ─────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BudgetComparison {
        private Category category;
        private BigDecimal budgetLimit;
        private BigDecimal actualSpent;
        private BigDecimal remaining;
        private double percentageUsed;
        private boolean isOverBudget;
    }

    // ── Full dashboard summary ──────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardSummary {
        private BigDecimal totalThisMonth;
        private BigDecimal totalLastMonth;
        private BigDecimal totalThisYear;
        private int totalExpensesThisMonth;
        private List<CategorySummary> categoryBreakdown;
        private List<MonthlyTotal> monthlyTotals;
        private List<BudgetComparison> budgetComparisons;
    }
}