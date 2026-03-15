package com.expensetrackaer.app.strategy;

import com.expensetrackaer.app.entity.model.*;
import com.expensetrackaer.app.repository.AlertRepository;
import com.expensetrackaer.app.repository.BudgetRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class BudgetWarningStrategy implements AlertStrategy{

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final BudgetRepository budgetRepository;

    @Autowired
    public BudgetWarningStrategy(TransactionRepository transactionRepository,AlertRepository alertRepository,BudgetRepository budgetRepository){
        this.transactionRepository=transactionRepository;
        this.alertRepository=alertRepository;
        this.budgetRepository=budgetRepository;
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

                    if (usage.compareTo(BigDecimal.valueOf(0.8)) >= 0
                            && usage.compareTo(BigDecimal.ONE) < 0) {

                        boolean exists =
                                alertRepository.existsAlertForMonth(
                                        userId,
                                        categoryId,
                                        AlertType.BUDGET_THRESHOLD,
                                        startDate.atStartOfDay(),
                                        endDate.atTime(23, 59, 59)
                                );

                        if (!exists) {

                            Alert alert = Alert.builder()
                                    .user(transaction.getUser())
                                    .category(transaction.getCategory())
                                    .alertType(AlertType.BUDGET_THRESHOLD)
                                    .message(
                                            "You have spent 80% of your "
                                                    + transaction.getCategory().getName()
                                                    + " budget"
                                    )
                                    .isRead(false)
                                    .build();

                            alertRepository.save(alert);
                        }
                    }
                });
    }
}
