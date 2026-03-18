package com.expensetrackaer.app.repository;

import com.expensetrackaer.app.entity.model.Transaction;
import com.expensetrackaer.app.entity.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> { // ✅ Added — enables Specification queries

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.category.id = :categoryId " +
            "AND t.transactionType = 'EXPENSE' " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getMonthlyExpenseTotal(
            Long userId,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT AVG(t.amount) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.transactionType = 'EXPENSE'")
    BigDecimal getAverageExpense(Long userId);

    // ✅ Removed findAllByFilters — replaced by TransactionSpecification
    // PostgreSQL could not determine the data type of null parameters in that query


    @Query("""
           SELECT t FROM Transaction t
           WHERE t.user.id = :userId
           AND FUNCTION('MONTH', t.transactionDate) = :month
           AND FUNCTION('YEAR', t.transactionDate) = :year
           ORDER BY t.transactionDate DESC
           """)
    List<Transaction> findTransactionsForExport(Long userId, int month, int year);


    @Query("""
           SELECT t FROM Transaction t
           WHERE t.user.id = :userId
           AND t.transactionDate BETWEEN :startDate AND :endDate
           ORDER BY t.transactionDate DESC
           """)
    List<Transaction> findTransactionsForExport(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    boolean existsByCategory_Id(Long id);
}