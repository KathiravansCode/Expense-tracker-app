package com.expensetrackaer.app.repository;

import com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse;
import com.expensetrackaer.app.entity.dto.SpendingTrendResponse;
import com.expensetrackaer.app.entity.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Transaction, Long> {

    // ✅ Removed (:startDate IS NULL OR ...) pattern — causes PostgreSQL type error
    // Dates are now always provided by AnalyticsServiceImpl — never null
    @Query("""
            SELECT new com.expensetrackaer.app.entity.dto.SpendingTrendResponse(
                t.transactionDate,
                SUM(t.amount)
            )
            FROM Transaction t
            WHERE t.user.id = :userId
            AND t.transactionType = 'EXPENSE'
            AND t.transactionDate >= :startDate
            AND t.transactionDate <= :endDate
            GROUP BY t.transactionDate
            ORDER BY t.transactionDate
            """)
    List<SpendingTrendResponse> getDailySpendingTrend(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ✅ Same fix applied here
    @Query("""
            SELECT new com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse(
                c.name,
                SUM(t.amount)
            )
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.id = :userId
            AND t.transactionType = 'EXPENSE'
            AND t.transactionDate >= :startDate
            AND t.transactionDate <= :endDate
            GROUP BY c.name
            """)
    List<CategoryBreakdownResponse> getCategoryBreakdown(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ✅ Same fix applied here
    @Query("""
            SELECT COALESCE(SUM(CASE WHEN t.transactionType = 'INCOME'  THEN t.amount ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN t.transactionType = 'EXPENSE' THEN t.amount ELSE 0 END), 0)
            FROM Transaction t
            WHERE t.user.id = :userId
            AND t.transactionDate >= :startDate
            AND t.transactionDate <= :endDate
            """)
    List<Object[]> getSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}