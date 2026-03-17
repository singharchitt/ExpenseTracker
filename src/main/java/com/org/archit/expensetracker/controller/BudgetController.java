package com.org.archit.expensetracker.controller;

import com.org.archit.expensetracker.dto.BudgetDTO;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(budgetService.getBudgetsByMonthAndYear(m, y));
    }

    // GET /api/budgets/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BudgetDTO.Response> getBudgetById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    // GET /api/budgets/category/{category}?month=3&year=2026
    @GetMapping("/category/{category}")
    public ResponseEntity<BudgetDTO.Response> getBudgetByCategory(
            @PathVariable Category category,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        return budgetService.getBudgetByCategoryAndMonth(category, m, y)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/budgets  (creates or updates)
    @PostMapping
    public ResponseEntity<BudgetDTO.Response> setBudget(
            @Valid @RequestBody BudgetDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.setbudget(request));
    }

    // PUT /api/budgets/{id}
    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO.Response> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDTO.Request request) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request));
    }

    // DELETE /api/budgets/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}