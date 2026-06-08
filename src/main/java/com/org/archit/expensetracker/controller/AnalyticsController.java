package com.org.archit.expensetracker.controller;

import com.org.archit.expensetracker.dto.AnalyticsDTO;
import com.org.archit.expensetracker.model.User;
import com.org.archit.expensetracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // GET /api/analytics/dashboard?month=3&year=2026
    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDTO.DashboardSummary> getDashboard(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(analyticsService.getDashboardSummary(user.getId(), m, y));
    }

    // GET /api/analytics/categories?month=3&year=2026
    @GetMapping("/categories")
    public ResponseEntity<List<AnalyticsDTO.CategorySummary>> getCategoryBreakdown(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(analyticsService.getCategoryBreakdown(user.getId(), m, y));
    }

    // GET /api/analytics/monthly?months=6
    @GetMapping("/monthly")
    public ResponseEntity<List<AnalyticsDTO.MonthlyTotal>> getMonthlyTotals(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getMonthlyTotals(user.getId(), months));
    }

    // GET /api/analytics/budgets?month=3&year=2026
    @GetMapping("/budgets")
    public ResponseEntity<List<AnalyticsDTO.BudgetComparison>> getBudgetComparisons(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(analyticsService.getBudgetComparisons(user.getId(), m, y));
    }
}
