package com.org.archit.expensetracker.controller;

import com.org.archit.expensetracker.dto.BudgetDTO;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.model.User;
import com.org.archit.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    // GET /api/budgets?month=3&year=2026
    @GetMapping
    public ResponseEntity<List<BudgetDTO.Response>> getBudgets(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(budgetService.getBudgetsByMonthAndYear(user.getId(), m, y));
    }

    // GET /api/budgets/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BudgetDTO.Response> getBudgetById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id, user.getId()));
    }

    // GET /api/budgets/category/{category}?month=3&year=2026
    @GetMapping("/category/{category}")
    public ResponseEntity<BudgetDTO.Response> getBudgetByCategory(
            @AuthenticationPrincipal User user,
            @PathVariable Category category,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        return budgetService.getBudgetByCategoryAndMonth(user.getId(), category, m, y)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/budgets
    @PostMapping
    public ResponseEntity<BudgetDTO.Response> setBudget(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BudgetDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.setBudget(request, user.getId()));
    }

    // PUT /api/budgets/{id}
    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO.Response> updateBudget(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody BudgetDTO.Request request) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request, user.getId()));
    }

    // DELETE /api/budgets/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        budgetService.deleteBudget(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
