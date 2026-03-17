package com.expensetrackaer.app.service;

import com.expensetrackaer.app.entity.dto.AlertResponse;
import com.expensetrackaer.app.entity.model.Alert;
import com.expensetrackaer.app.entity.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AlertService {

    // ✅ Removed userId params — resolved from JWT inside the impl
    Page<AlertResponse> getAlerts(Pageable pageable);

    void markAlertAsRead(Long alertId);

    void checkAlerts(Transaction transaction);

    void reEvaluateBudgetAlerts(Long userId, Long categoryId, LocalDate date);

    void pushAlertToUser(Long userId, Alert alert);
}