package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.AlertResponse;
import com.expensetrackaer.app.entity.model.Alert;
import com.expensetrackaer.app.entity.model.AlertType;
import com.expensetrackaer.app.entity.model.Transaction;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.AlertRepository;
import com.expensetrackaer.app.repository.BudgetRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.service.AlertService;
import com.expensetrackaer.app.strategy.AlertStrategy;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final List<AlertStrategy> alertStrategies;

    public AlertServiceImpl(AlertRepository alertRepository,TransactionRepository transactionRepository,BudgetRepository budgetRepository,List<AlertStrategy> alertStrategies){
        this.alertRepository=alertRepository;
        this.transactionRepository=transactionRepository;
        this.budgetRepository=budgetRepository;
        this.alertStrategies=alertStrategies;
    }

    @Override
    public Page<AlertResponse> getAlerts(Long userId, Pageable pageable) {

        Page<Alert> alerts =
                alertRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return alerts.map(this::mapToResponse);
    }

    @Override
    public void markAlertAsRead(Long alertId, Long userId) {
        Alert alert = alertRepository
                .findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        alert.setIsRead(true);

        alertRepository.save(alert);


    }

    @Override
    public void checkAlerts(Transaction transaction) {

        for (AlertStrategy strategy : alertStrategies) {
            strategy.check(transaction);
        }
    }

    @Override
    public void reEvaluateBudgetAlerts(Long userId, Long categoryId, LocalDate date) {
        LocalDate startDate = date.withDayOfMonth(1);
        LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());

        alertRepository.deleteBudgetAlerts(
                userId,
                categoryId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
    }

    private void createAlert(
            Transaction transaction,
            AlertType type,
            String message
    ) {

        Alert alert = Alert.builder()
                .alertType(type)
                .message(message)
                .user(transaction.getUser())
                .category(transaction.getCategory())
                .isRead(false)
                .build();

        alertRepository.save(alert);
    }

    private AlertResponse mapToResponse(Alert alert) {

        return AlertResponse.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType())
                .message(alert.getMessage())
                .isRead(alert.getIsRead())
                .categoryName( alert.getCategory() != null
                        ? alert.getCategory().getName()
                        : null)
                .createdAt(alert.getCreatedAt())
                .build();
    }

}

