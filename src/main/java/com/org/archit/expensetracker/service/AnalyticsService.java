package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.AnalyticsDTO;

import java.util.List;

public interface AnalyticsService {
    AnalyticsDTO.DashboardSummary getDashboardSummary(Long userId, int month, int year);
    List<AnalyticsDTO.CategorySummary> getCategoryBreakdown(Long userId, int month, int year);
    List<AnalyticsDTO.MonthlyTotal> getMonthlyTotals(Long userId, int months);
    List<AnalyticsDTO.BudgetComparison> getBudgetComparisons(Long userId, int month, int year);
}
