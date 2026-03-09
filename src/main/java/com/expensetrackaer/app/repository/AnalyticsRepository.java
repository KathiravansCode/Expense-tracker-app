package com.expensetrackaer.app.repository;

import com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse;
import com.expensetrackaer.app.entity.dto.SpendingTrendResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnalyticsRepository {
    @Query("""
    SELECT new com.expensetrackaer.app.entity.dto.SpendingTrendResponse(
        t.transactionDate,
        SUM(t.amount)
    )
    FROM Transaction t
    WHERE t.user.id = :userId
    AND t.transactionType = 'EXPENSE'
    AND (:startDate IS NULL OR t.transactionDate >= :startDate)
    AND (:endDate IS NULL OR t.transactionDate <= :endDate)
    GROUP BY t.transactionDate
    ORDER BY t.transactionDate
""")
    List<SpendingTrendResponse> getDailySpendingTrend(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
                SELECT new com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse(
                    c.name,
                    SUM(t.amount)
                )
                FROM Transaction t
                JOIN t.category c
                WHERE t.user.id = :userId
                AND t.transactionType = 'EXPENSE'
                AND (:startDate IS NULL OR t.transactionDate >= :startDate)
                AND (:endDate IS NULL OR t.transactionDate <= :endDate)
                GROUP BY c.name
            """)
    List<CategoryBreakdownResponse> getCategoryBreakdown(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
                SELECT COALESCE(SUM(CASE WHEN t.transactionType='INCOME' THEN t.amount ELSE 0 END),0),
                       COALESCE(SUM(CASE WHEN t.transactionType='EXPENSE' THEN t.amount ELSE 0 END),0)
                FROM Transaction t
                WHERE t.user.id = :userId
                AND (:startDate IS NULL OR t.transactionDate >= :startDate)
                AND (:endDate IS NULL OR t.transactionDate <= :endDate)
            """)
    Object[] getSummary(Long userId, LocalDate startDate, LocalDate endDate);




}
