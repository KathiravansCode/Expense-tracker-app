package com.expensetrackaer.app.service;


import com.expensetrackaer.app.entity.dto.AlertResponse;
import com.expensetrackaer.app.entity.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AlertService {


    Page<AlertResponse> getAlerts(Long userId, Pageable pageable);

    void markAlertAsRead(Long alertId, Long userId);

    void checkAlerts(Transaction transaction);


    void reEvaluateBudgetAlerts(Long userId, Long categoryId, LocalDate date);
}
