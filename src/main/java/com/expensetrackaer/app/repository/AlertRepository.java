package com.expensetrackaer.app.repository;

import com.expensetrackaer.app.entity.model.Alert;
import com.expensetrackaer.app.entity.model.AlertType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Alert> findByIdAndUserId(Long id, Long userId);

    /*
      Used to prevent duplicate alerts
     */
    @Query("""
           SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
           FROM Alert a
           WHERE a.user.id = :userId
           AND a.category.id = :categoryId
           AND a.alertType = :alertType
           AND a.createdAt BETWEEN :startDate AND :endDate
           """)
    boolean existsAlertForMonth(
            Long userId,
            Long categoryId,
            AlertType alertType,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /*
      Used when recalculating alerts after transaction delete/update
     */
    @Modifying
    @Query("""
           DELETE FROM Alert a
           WHERE a.user.id = :userId
           AND a.category.id = :categoryId
           AND a.alertType IN ('BUDGET_THRESHOLD','BUDGET_EXCEEDED')
           AND a.createdAt BETWEEN :startDate AND :endDate
           """)
    void deleteBudgetAlerts(
            Long userId,
            Long categoryId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
