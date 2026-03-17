package com.org.archit.expensetracker.controller;

import com.org.archit.expensetracker.dto.AnalyticsDTO;
import com.org.archit.expensetracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // GET /api/analytics/dashboard?month=3&year=2026
    // Returns full summary: totals, category breakdown, monthly chart, budget comparisons
    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDTO.DashboardSummary> getDashboard(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(analyticsService.getDashboardSummary(m, y));
    }

    // GET /api/analytics/categories?month=3&year=2026
    // Returns spending breakdown by category with percentages (for pie chart)
    @GetMapping("/categories")
    public ResponseEntity<List<AnalyticsDTO.CategorySummary>> getCategoryBreakdown(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(analyticsService.getCategoryBreakdown(m, y));
    }

    // GET /api/analytics/monthly?months=6
    // Returns monthly totals for the last N months (for bar/line chart)
    @GetMapping("/monthly")
    public ResponseEntity<List<AnalyticsDTO.MonthlyTotal>> getMonthlyTotals(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getMonthlyTotals(months));
    }

    // GET /api/analytics/budgets?month=3&year=2026
    // Returns budget vs actual comparison per category
    @GetMapping("/budgets")
    public ResponseEntity<List<AnalyticsDTO.BudgetComparison>> getBudgetComparisons(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(analyticsService.getBudgetComparisons(m, y));
    }
}