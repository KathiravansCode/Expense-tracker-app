package com.expensetrackaer.app.strategy;

import com.expensetrackaer.app.entity.dto.AlertResponse;
import com.expensetrackaer.app.entity.model.Alert;
import com.expensetrackaer.app.entity.model.AlertType;
import com.expensetrackaer.app.entity.model.Transaction;
import com.expensetrackaer.app.entity.model.TransactionType;
import com.expensetrackaer.app.repository.AlertRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.service.AlertService;
import com.expensetrackaer.app.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component

public class UnusualExpenseStrategy implements AlertStrategy{

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final SseEmitterService sseEmitterService;
    @Autowired
    public UnusualExpenseStrategy(TransactionRepository transactionRepository,AlertRepository alertRepository,SseEmitterService sseEmitterService){
        this.transactionRepository=transactionRepository;
        this.alertRepository=alertRepository;
        this.sseEmitterService=sseEmitterService;
    }

    @Override
    public void check(Transaction transaction) {

        if(transaction.getTransactionType() != TransactionType.EXPENSE){
            return;
        }

        BigDecimal avg = transactionRepository
                .getAverageExpense(transaction.getUser().getId());

        BigDecimal threshold = avg.multiply(BigDecimal.valueOf(2));

        if(transaction.getAmount().compareTo(threshold) > 0){

            Long userId = transaction.getUser().getId(); // ← extract from transaction

            transaction.setIsUnusual(true); // ← mark transaction as unusual
            transactionRepository.save(transaction);

            Alert alert = Alert.builder()
                    .alertType(AlertType.UNUSUAL_EXPENSE)
                    .message("Unusual expense detected")
                    .user(transaction.getUser())
                    .isRead(false) // ← not nullable in DB, must be set
                    .build();

            Alert saved = alertRepository.save(alert);

            AlertResponse response = AlertResponse.builder()
                    .id(saved.getId())
                    .alertType(saved.getAlertType())
                    .message(saved.getMessage())
                    .isRead(saved.getIsRead())
                    .categoryName(null) // ← UnusualExpense has no specific category
                    .createdAt(saved.getCreatedAt())
                    .build();

            sseEmitterService.sendAlert(userId, response);
        }
    }
}
