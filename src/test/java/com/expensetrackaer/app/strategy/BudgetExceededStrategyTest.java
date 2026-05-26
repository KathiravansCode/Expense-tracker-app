package com.expensetrackaer.app.strategy;

import com.expensetrackaer.app.entity.model.*;
import com.expensetrackaer.app.repository.AlertRepository;
import com.expensetrackaer.app.repository.BudgetRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.service.SseEmitterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetExceededStrategyTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private SseEmitterService sseEmitterService;

    @Captor private ArgumentCaptor<Alert> alertCaptor;

    @Test
    void check_whenUsageOver100Percent_createsAlertAndPushes() {
        BudgetExceededStrategy strategy = new BudgetExceededStrategy(
                transactionRepository, alertRepository, budgetRepository, sseEmitterService
        );

        LocalDate date = LocalDate.of(2026, 3, 10);
        User user = User.builder().id(1L).build();
        Category category = Category.builder().id(2L).name("Food").build();
        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(date)
                .amount(new BigDecimal("10.00"))
                .user(user)
                .category(category)
                .build();

        Budget budget = Budget.builder()
                .limitAmount(new BigDecimal("100.00"))
                .month(Month.MARCH)
                .year(2026)
                .user(user)
                .category(category)
                .build();

        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(1L, 2L, Month.MARCH, 2026))
                .thenReturn(Optional.of(budget));
        when(transactionRepository.getMonthlyExpenseTotal(eq(1L), eq(2L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("120.00"));
        when(alertRepository.existsAlertForMonth(eq(1L), eq(2L), eq(AlertType.BUDGET_EXCEEDED), any(), any()))
                .thenReturn(false);
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> {
            Alert a = inv.getArgument(0);
            a.setId(100L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        strategy.check(tx);

        verify(alertRepository).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getAlertType()).isEqualTo(AlertType.BUDGET_EXCEEDED);
        verify(sseEmitterService).sendAlert(eq(1L), any());
    }

    @Test
    void check_whenExactly100Percent_doesNotCreateExceededAlert() {
        BudgetExceededStrategy strategy = new BudgetExceededStrategy(
                transactionRepository, alertRepository, budgetRepository, sseEmitterService
        );

        LocalDate date = LocalDate.of(2026, 3, 10);
        User user = User.builder().id(1L).build();
        Category category = Category.builder().id(2L).name("Food").build();
        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.EXPENSE)
                .transactionDate(date)
                .amount(new BigDecimal("10.00"))
                .user(user)
                .category(category)
                .build();

        Budget budget = Budget.builder()
                .limitAmount(new BigDecimal("100.00"))
                .month(Month.MARCH)
                .year(2026)
                .user(user)
                .category(category)
                .build();

        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(1L, 2L, Month.MARCH, 2026))
                .thenReturn(Optional.of(budget));
        when(transactionRepository.getMonthlyExpenseTotal(eq(1L), eq(2L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("100.00"));

        strategy.check(tx);

        verify(alertRepository, never()).save(any());
        verifyNoInteractions(sseEmitterService);
    }
}

