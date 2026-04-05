package com.finance.system.dashboard.service;

import com.finance.system.dashboard.dto.DashboardDtos.*;
import com.finance.system.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.finance.system.config.RedisConfig.DASHBOARD_CACHE;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    /**
     * Returns the full dashboard summary for a user.
     * Cache key: dashboard:user:{userId}  (TTL from application.properties)
     */
    @Cacheable(
        cacheNames = DASHBOARD_CACHE,
        key = "'dashboard:user:' + #userId"
    )
    @Transactional(readOnly = true)
    public DashboardSummary getDashboardSummary(Long userId) {
        log.info("Building dashboard summary for user={} (cache miss)", userId);

        BigDecimal totalIncome  = recordRepository.sumIncomeByUserId(userId);
        BigDecimal totalExpense = recordRepository.sumExpenseByUserId(userId);
        BigDecimal netBalance   = totalIncome.subtract(totalExpense);

        List<CategorySummary> categories = buildCategorySummary(userId);
        List<MonthlyTrend>    trends     = buildMonthlyTrends(userId);

        return DashboardSummary.builder()
                .userId(userId)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .categorySummary(categories)
                .monthlyTrends(trends)
                .generatedAt(LocalDateTime.now().toString())
                .build();
    }

    // ── Private Builders ──────────────────────────────────────────────────

    private List<CategorySummary> buildCategorySummary(Long userId) {
        List<Object[]> rows = recordRepository.getCategorySummaryByUserId(userId);
        List<CategorySummary> result = new ArrayList<>();

        for (Object[] row : rows) {
            result.add(CategorySummary.builder()
                    .category((String) row[0])
                    .type(row[1].toString())
                    .total((BigDecimal) row[2])
                    .build());
        }
        return result;
    }

    private List<MonthlyTrend> buildMonthlyTrends(Long userId) {
        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusMonths(11).withDayOfMonth(1);   // rolling 12 months

        List<Object[]> rows = recordRepository.getMonthlyTrends(userId, from, to);
        List<MonthlyTrend> result = new ArrayList<>();

        for (Object[] row : rows) {
            result.add(MonthlyTrend.builder()
                    .year(((Number) row[0]).intValue())
                    .month(((Number) row[1]).intValue())
                    .type((String) row[2])
                    .total((BigDecimal) row[3])
                    .build());
        }
        return result;
    }
}
