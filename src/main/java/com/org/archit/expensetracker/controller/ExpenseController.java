package com.org.archit.expensetracker.controller;

import com.org.archit.expensetracker.dto.ExpenseDTO;
import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.model.User;
import com.org.archit.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // GET /api/expenses?page=0&size=20
    // GET /api/expenses?month=3&year=2026
    // GET /api/expenses?category=FOOD
    // GET /api/expenses?start=2026-01-01&end=2026-03-31
    @GetMapping
    public ResponseEntity<?> getExpenses(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        if (month != null && year != null) {
            List<ExpenseDTO.Response> result = expenseService.getExpensesByMonthAndYear(user.getId(), month, year);
            return ResponseEntity.ok(result);
        } else if (category != null) {
            Page<ExpenseDTO.Response> result = expenseService.getExpensesByCategory(user.getId(), category, pageable);
            return ResponseEntity.ok(result);
        } else if (start != null && end != null) {
            Page<ExpenseDTO.Response> result = expenseService.getExpensesByDateRange(user.getId(), start, end, pageable);
            return ResponseEntity.ok(result);
        } else {
            Page<ExpenseDTO.Response> result = expenseService.getAllExpenses(user.getId(), pageable);
            return ResponseEntity.ok(result);
        }
    }

    // GET /api/expenses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO.Response> getExpenseById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id, user.getId()));
    }

    // POST /api/expenses
    @PostMapping
    public ResponseEntity<ExpenseDTO.Response> createExpense(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ExpenseDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(request, user.getId()));
    }

    // PUT /api/expenses/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO.Response> updateExpense(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO.Request request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request, user.getId()));
    }

    // DELETE /api/expenses/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        expenseService.deleteExpense(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
