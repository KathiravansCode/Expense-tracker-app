package com.expensetrackaer.app.strategy;

import com.expensetrackaer.app.entity.model.Alert;
import com.expensetrackaer.app.entity.model.AlertType;
import com.expensetrackaer.app.entity.model.Transaction;
import com.expensetrackaer.app.entity.model.TransactionType;
import com.expensetrackaer.app.repository.AlertRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component

public class UnusualExpenseStrategy implements AlertStrategy{

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;

    @Autowired
    public UnusualExpenseStrategy(TransactionRepository transactionRepository,AlertRepository alertRepository){
        this.transactionRepository=transactionRepository;
        this.alertRepository=alertRepository;
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

            Alert alert = Alert.builder()
                    .alertType(AlertType.UNUSUAL_EXPENSE)
                    .message("Unusual expense detected")
                    .user(transaction.getUser())
                    .build();

            alertRepository.save(alert);
        }
    }
}
