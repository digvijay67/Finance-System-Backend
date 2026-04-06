package com.finance.system.repository;

import com.finance.system.entity.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>,
        JpaSpecificationExecutor<FinancialRecord> {

    List<FinancialRecord> findByUserId(Long userId);

    // ── Dashboard Aggregations ──────────────────────────────────────────────

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.user.id = :userId AND r.type = 'INCOME'
    """)
    BigDecimal sumIncomeByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.user.id = :userId AND r.type = 'EXPENSE'
    """)
    BigDecimal sumExpenseByUserId(@Param("userId") Long userId);

    // ── Category Summary ────────────────────────────────────────────────────

    @Query("""
        SELECT r.category, r.type, COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.user.id = :userId
        GROUP BY r.category, r.type
        ORDER BY r.category
    """)
    List<Object[]> getCategorySummaryByUserId(@Param("userId") Long userId);

    // ── Monthly Trends ──────────────────────────────────────────────────────

    @Query(value = """
        SELECT
            EXTRACT(YEAR  FROM record_date) AS year,
            EXTRACT(MONTH FROM record_date) AS month,
            type,
            COALESCE(SUM(amount), 0)         AS total
        FROM financial_records
        WHERE user_id = :userId
          AND record_date >= :from
          AND record_date <= :to
        GROUP BY year, month, type
        ORDER BY year, month, type
    """, nativeQuery = true)
    List<Object[]> getMonthlyTrends(
            @Param("userId") Long userId,
            @Param("from")   LocalDate from,
            @Param("to")     LocalDate to);

    // ── Filtered Queries ────────────────────────────────────────────────────

    @Query("""
        SELECT r FROM FinancialRecord r
        WHERE r.user.id = :userId
          AND (:type     IS NULL OR r.type     = :type)
          AND (:category IS NULL OR r.category = :category)
          AND (:from     IS NULL OR r.date    >= :from)
          AND (:to       IS NULL OR r.date    <= :to)
        ORDER BY r.date DESC
    """)
    List<FinancialRecord> findByFilters(
            @Param("userId")   Long userId,
            @Param("type")     FinancialRecord.RecordType type,
            @Param("category") String category,
            @Param("from")     LocalDate from,
            @Param("to")       LocalDate to);

    boolean existsByIdAndUserId(Long id, Long userId);
}
