package com.expensetrackaer.app.strategy;

import com.expensetrackaer.app.entity.model.*;
import com.expensetrackaer.app.repository.AlertRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.service.SseEmitterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnusualExpenseStrategyTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private SseEmitterService sseEmitterService;

    @Captor private ArgumentCaptor<Transaction> txCaptor;
    @Captor private ArgumentCaptor<Alert> alertCaptor;

    @Test
    void check_nonExpense_doesNothing() {
        UnusualExpenseStrategy strategy = new UnusualExpenseStrategy(
                transactionRepository, alertRepository, sseEmitterService
        );

        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.INCOME)
                .build();

        strategy.check(tx);

        verifyNoInteractions(transactionRepository, alertRepository, sseEmitterService);
    }

    @Test
    void check_whenAmountGreaterThan2xAverage_marksUnusual_savesAlert_andPushes() {
        UnusualExpenseStrategy strategy = new UnusualExpenseStrategy(
                transactionRepository, alertRepository, sseEmitterService
        );

        User user = User.builder().id(1L).build();
        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.EXPENSE)
                .amount(new BigDecimal("25.00"))
                .user(user)
                .isUnusual(false)
                .build();

        when(transactionRepository.getAverageExpense(1L)).thenReturn(new BigDecimal("10.00"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> {
            Alert a = inv.getArgument(0);
            a.setId(5L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        strategy.check(tx);

        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getIsUnusual()).isTrue();

        verify(alertRepository).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getAlertType()).isEqualTo(AlertType.UNUSUAL_EXPENSE);
        assertThat(alertCaptor.getValue().getIsRead()).isFalse();

        verify(sseEmitterService).sendAlert(eq(1L), any());
    }
}

