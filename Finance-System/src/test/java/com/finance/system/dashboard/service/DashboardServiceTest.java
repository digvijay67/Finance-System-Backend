package com.finance.system.dashboard.service;

import com.finance.system.dashboard.dto.DashboardDtos.CategorySummary;
import com.finance.system.dashboard.dto.DashboardDtos.DashboardSummary;
import com.finance.system.dashboard.dto.DashboardDtos.MonthlyTrend;
import com.finance.system.entity.FinancialRecord;
import com.finance.system.repository.FinancialRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardSummary_shouldReturnAggregatedSummary() {
        when(recordRepository.sumIncomeByUserId(1L)).thenReturn(new BigDecimal("1000.00"));
        when(recordRepository.sumExpenseByUserId(1L)).thenReturn(new BigDecimal("250.00"));

        List<Object[]> categoryRows = new ArrayList<>();
        categoryRows.add(new Object[]{"Sales", FinancialRecord.RecordType.INCOME, new BigDecimal("1000.00")});
        when(recordRepository.getCategorySummaryByUserId(1L)).thenReturn(categoryRows);

        List<Object[]> trendRows = new ArrayList<>();
        trendRows.add(new Object[]{2026, 4, "INCOME", new BigDecimal("1000.00")});
        when(recordRepository.getMonthlyTrends(eq(1L), any(java.time.LocalDate.class), any(java.time.LocalDate.class))).thenReturn(trendRows);

        DashboardSummary summary = dashboardService.getDashboardSummary(1L);

        assertThat(summary.getUserId()).isEqualTo(1L);
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(summary.getTotalExpense()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(summary.getNetBalance()).isEqualByComparingTo(new BigDecimal("750.00"));
        assertThat(summary.getCategorySummary()).hasSize(1);
        assertThat(summary.getMonthlyTrends()).hasSize(1);

        CategorySummary category = summary.getCategorySummary().get(0);
        assertThat(category.getCategory()).isEqualTo("Sales");
        assertThat(category.getType()).isEqualTo("INCOME");
        assertThat(category.getTotal()).isEqualByComparingTo(new BigDecimal("1000.00"));

        MonthlyTrend trend = summary.getMonthlyTrends().get(0);
        assertThat(trend.getYear()).isEqualTo(2026);
        assertThat(trend.getMonth()).isEqualTo(4);
        assertThat(trend.getType()).isEqualTo("INCOME");
        assertThat(trend.getTotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }
}
