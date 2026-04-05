package com.finance.system.records.dto;

import com.finance.system.entity.FinancialRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class RecordDtos {

    private RecordDtos() {}

    // ── Create / Update Request ───────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Create or update a financial record")
    public static class RecordRequest {

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
        @Schema(example = "1500.00")
        private BigDecimal amount;

        @NotNull(message = "Type is required")
        @Schema(example = "INCOME")
        private FinancialRecord.RecordType type;

        @NotBlank(message = "Category is required")
        @Size(max = 100, message = "Category must be at most 100 characters")
        @Schema(example = "Salary")
        private String category;

        @NotNull(message = "Date is required")
        @Schema(example = "2024-06-15")
        private LocalDate date;

        @Size(max = 500, message = "Description must be at most 500 characters")
        @Schema(example = "Monthly salary from employer")
        private String description;
    }

    // ── Record Response ───────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Financial record response")
    public static class RecordResponse {
        private Long id;
        private BigDecimal amount;
        private String type;
        private String category;
        private LocalDate date;
        private String description;
        private Long userId;
        private String userEmail;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // ── Filter Request ────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Filter parameters for financial records")
    public static class RecordFilterRequest {

        @Schema(example = "INCOME", description = "Filter by record type")
        private FinancialRecord.RecordType type;

        @Schema(example = "Salary", description = "Filter by category")
        private String category;

        @Schema(example = "2024-01-01", description = "Start date (inclusive)")
        private LocalDate from;

        @Schema(example = "2024-12-31", description = "End date (inclusive)")
        private LocalDate to;
    }
}
