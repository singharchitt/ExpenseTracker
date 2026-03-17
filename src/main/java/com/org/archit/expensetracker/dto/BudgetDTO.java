package com.org.archit.expensetracker.dto;

import com.org.archit.expensetracker.model.Category;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BudgetDTO {

    // ── Request DTO ─────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotNull(message = "Category is required")
        private Category category;

        @NotNull(message = "Budget limit is required")
        @DecimalMin(value = "0.01", message = "Budget must be greater than 0")
        @Digits(integer = 10, fraction = 2)
        private BigDecimal budgetLimit;

        @NotNull(message = "Month is required")
        @Min(1) @Max(12)
        private Integer month;

        @NotNull(message = "Year is required")
        @Min(2000)
        private Integer year;
    }

    // ── Response DTO ────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Category category;
        private BigDecimal budgetLimit;
        private Integer month;
        private Integer year;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}