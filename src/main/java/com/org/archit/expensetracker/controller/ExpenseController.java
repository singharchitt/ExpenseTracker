package com.org.archit.expensetracker.controller;

import com.org.archit.expensetracker.dto.ExpenseDTO;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // GET /api/expenses
    // GET /api/expenses?month=3&year=2026
    // GET /api/expenses?category=FOOD
    // GET /api/expenses?start=2026-01-01&end=2026-03-31
    @GetMapping
    public ResponseEntity<List<ExpenseDTO.Response>> getExpenses(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<ExpenseDTO.Response> result;

        if (month != null && year != null) {
            result = expenseService.getExpensesByMonthAndYear(month, year);
        } else if (category != null) {
            result = expenseService.getExpensesByCategory(category);
        } else if (start != null && end != null) {
            result = expenseService.getExpensesByDateRange(start, end);
        } else {
            result = expenseService.getAllExpenses();
        }

        return ResponseEntity.ok(result);
    }

    // GET /api/expenses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO.Response> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    // POST /api/expenses
    @PostMapping
    public ResponseEntity<ExpenseDTO.Response> createExpense(
            @Valid @RequestBody ExpenseDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(request));
    }

    // PUT /api/expenses/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO.Response> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO.Request request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    // DELETE /api/expenses/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}