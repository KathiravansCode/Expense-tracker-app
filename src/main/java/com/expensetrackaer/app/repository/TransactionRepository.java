package com.expensetrackaer.app.repository;

import com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse;
import com.expensetrackaer.app.entity.model.Transaction;
import com.expensetrackaer.app.entity.model.TransactionType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId AND t.transactionType = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getMonthlyExpenseTotal(
            Long userId,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.transactionType = 'EXPENSE'")
    BigDecimal getAverageExpense(Long userId);

    @Query("""
                SELECT t FROM Transaction t
                WHERE t.user.id = :userId
                AND (:type IS NULL OR t.transactionType = :type)
                AND (:startDate IS NULL OR t.transactionDate >= :startDate)
                AND (:endDate IS NULL OR t.transactionDate <= :endDate)
            """)
    Page<Transaction> findAllByFilters(
            Long userId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    @Query("""
       SELECT t FROM Transaction t
       WHERE t.user.id = :userId
       AND FUNCTION('MONTH', t.transactionDate) = :month
       AND FUNCTION('YEAR', t.transactionDate) = :year
       ORDER BY t.transactionDate DESC
       """)
    List<Transaction> findTransactionsForExport(
            Long userId,
            int month,
            int year
    );//this should be checked



    boolean existsByCategory_Id(Long id);

}

