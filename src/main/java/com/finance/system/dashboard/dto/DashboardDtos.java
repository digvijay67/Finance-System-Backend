package com.finance.system.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {}

    // ── Full Summary ──────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Complete dashboard summary for a user")
    public static class DashboardSummary implements Serializable {

        private Long userId;

        @Schema(example = "45000.00")
        private BigDecimal totalIncome;

        @Schema(example = "28000.00")
        private BigDecimal totalExpense;

        @Schema(example = "17000.00")
        private BigDecimal netBalance;

        private List<CategorySummary> categorySummary;
        private List<MonthlyTrend>    monthlyTrends;

        @Schema(example = "2024-06-15T10:30:00")
        private String generatedAt;
    }

    // ── Category Summary ──────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Aggregated totals per category and type")
    public static class CategorySummary implements Serializable {

        @Schema(example = "Salary")
        private String category;

        @Schema(example = "INCOME")
        private String type;

        @Schema(example = "40000.00")
        private BigDecimal total;
    }

    // ── Monthly Trend ─────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Monthly income/expense trend")
    public static class MonthlyTrend implements Serializable {

        @Schema(example = "2024")
        private int year;

        @Schema(example = "6")
        private int month;

        @Schema(example = "INCOME")
        private String type;

        @Schema(example = "5000.00")
        private BigDecimal total;
    }
}
