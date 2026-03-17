package com.expensetrackaer.app.strategy;

import com.expensetrackaer.app.entity.dto.AlertResponse;
import com.expensetrackaer.app.entity.model.*;
import com.expensetrackaer.app.repository.AlertRepository;
import com.expensetrackaer.app.repository.BudgetRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.service.AlertService;
import com.expensetrackaer.app.service.SseEmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class BudgetExceededStrategy implements AlertStrategy{
    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final BudgetRepository budgetRepository;
    private final SseEmitterService sseEmitterService;
    @Autowired
    public BudgetExceededStrategy(TransactionRepository transactionRepository,AlertRepository alertRepository,BudgetRepository budgetRepository,SseEmitterService sseEmitterService){
        this.transactionRepository=transactionRepository;
        this.alertRepository=alertRepository;
        this.budgetRepository=budgetRepository;
        this.sseEmitterService=sseEmitterService;
    }
    @Override
    public void check(Transaction transaction) {

        if (transaction.getTransactionType() != TransactionType.EXPENSE) {
            return;
        }

        Long userId = transaction.getUser().getId();
        Long categoryId = transaction.getCategory().getId();

        LocalDate date = transaction.getTransactionDate();

        Month month = Month.values()[date.getMonthValue() - 1];
        int year = date.getYear();

        budgetRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(
                        userId,
                        categoryId,
                        month,
                        year
                )
                .ifPresent(budget -> {

                    LocalDate startDate = date.withDayOfMonth(1);
                    LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());

                    BigDecimal totalExpense =
                            transactionRepository.getMonthlyExpenseTotal(
                                    userId,
                                    categoryId,
                                    startDate,
                                    endDate
                            );

                    BigDecimal usage =
                            totalExpense.divide(
                                    budget.getLimitAmount(),
                                    2,
                                    BigDecimal.ROUND_HALF_UP
                            );

                    if (usage.compareTo(BigDecimal.ONE) > 0) {

                        boolean exists =
                                alertRepository.existsAlertForMonth(
                                        userId,
                                        categoryId,
                                        AlertType.BUDGET_EXCEEDED,
                                        startDate.atStartOfDay(),
                                        endDate.atTime(23, 59, 59)
                                );

                        if (!exists) {

                            Alert alert = Alert.builder()
                                    .user(transaction.getUser())
                                    .category(transaction.getCategory())
                                    .alertType(AlertType.BUDGET_EXCEEDED)
                                    .message(
                                            "Budget exceeded for "
                                                    + transaction.getCategory().getName()
                                    )
                                    .isRead(false)
                                    .build();

                           Alert saved= alertRepository.save(alert);

                            AlertResponse response = AlertResponse.builder()
                                    .id(saved.getId())
                                    .alertType(saved.getAlertType())
                                    .message(saved.getMessage())
                                    .isRead(saved.getIsRead())
                                    .categoryName(transaction.getCategory().getName())
                                    .createdAt(saved.getCreatedAt())
                                    .build();

                            sseEmitterService.sendAlert(userId, response);
                        }
                    }
                });
    }
}
