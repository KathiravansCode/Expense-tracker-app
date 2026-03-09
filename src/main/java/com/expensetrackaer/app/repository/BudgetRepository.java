package com.expensetrackaer.app.repository;

import com.expensetrackaer.app.entity.model.Budget;
import com.expensetrackaer.app.entity.model.Month;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget,Long> {

    List<Budget> findByUserId(Long id);

    Optional<Budget> findByUserIdAndMonthAndYear(Long id,Month month,Integer year);

    Optional<Budget> findByIdAndUserId(Long id,Long userId);

    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Month month,Integer year);

    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Month month,Integer year);
}
