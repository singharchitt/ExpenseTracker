package com.org.archit.expensetracker.dto;

import com.org.archit.expensetracker.model.Category;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseDTO {

    // ── Request DTO (for create / update) ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "Title is required")
        @Size(max = 255)
        private String title;

        @Size(max = 500)
        private String description;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 10, fraction = 2)
        private BigDecimal amount;

        @NotNull(message = "Category is required")
        private Category category;

        @NotNull(message = "Date is required")
        private LocalDate date;
    }

    // ── Response DTO (returned to client) ──────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private BigDecimal amount;
        private Category category;
        private LocalDate date;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}