package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.AlertResponse;
import com.expensetrackaer.app.entity.model.*;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.AlertRepository;
import com.expensetrackaer.app.repository.BudgetRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.service.SseEmitterService;
import com.expensetrackaer.app.strategy.AlertStrategy;
import com.expensetrackaer.app.testutil.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock private AlertRepository alertRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private SseEmitterService sseEmitterService;
    @Mock private AlertStrategy strategy1;
    @Mock private AlertStrategy strategy2;

    @Captor private ArgumentCaptor<Alert> alertCaptor;
    @Captor private ArgumentCaptor<AlertResponse> responseCaptor;

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void getAlerts_usesCurrentUserId() {
        SecurityTestUtils.setAuthenticatedUser(10L, "a@example.com");
        Pageable pageable = PageRequest.of(0, 5);

        Alert alert = Alert.builder()
                .id(1L)
                .alertType(AlertType.UNUSUAL_EXPENSE)
                .message("m")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .user(User.builder().id(10L).build())
                .build();

        when(alertRepository.findByUserIdOrderByCreatedAtDesc(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(alert), pageable, 1));

        AlertServiceImpl service = new AlertServiceImpl(
                alertRepository,
                transactionRepository,
                budgetRepository,
                List.of(strategy1, strategy2),
                sseEmitterService
        );

        Page<AlertResponse> page = service.getAlerts(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getMessage()).isEqualTo("m");
    }

    @Test
    void markAlertAsRead_notFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(10L, "a@example.com");
        when(alertRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.empty());

        AlertServiceImpl service = new AlertServiceImpl(
                alertRepository,
                transactionRepository,
                budgetRepository,
                List.of(strategy1),
                sseEmitterService
        );

        assertThatThrownBy(() -> service.markAlertAsRead(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Alert not found");
    }

    @Test
    void markAlertAsRead_setsIsReadTrue_andSaves() {
        SecurityTestUtils.setAuthenticatedUser(10L, "a@example.com");
        Alert alert = Alert.builder()
                .id(1L)
                .alertType(AlertType.UNUSUAL_EXPENSE)
                .message("m")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .user(User.builder().id(10L).build())
                .build();
        when(alertRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertServiceImpl service = new AlertServiceImpl(
                alertRepository,
                transactionRepository,
                budgetRepository,
                List.of(strategy1),
                sseEmitterService
        );

        service.markAlertAsRead(1L);

        verify(alertRepository).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getIsRead()).isTrue();
    }

    @Test
    void checkAlerts_runsAllStrategies() {
        AlertServiceImpl service = new AlertServiceImpl(
                alertRepository,
                transactionRepository,
                budgetRepository,
                List.of(strategy1, strategy2),
                sseEmitterService
        );

        Transaction tx = Transaction.builder()
                .id(1L)
                .user(User.builder().id(10L).build())
                .category(Category.builder().id(2L).build())
                .transactionDate(LocalDate.now())
                .transactionType(TransactionType.EXPENSE)
                .amount(java.math.BigDecimal.ONE)
                .build();

        service.checkAlerts(tx);

        verify(strategy1).check(tx);
        verify(strategy2).check(tx);
    }

    @Test
    void reEvaluateBudgetAlerts_deletesAlertsForMonthRange() {
        AlertServiceImpl service = new AlertServiceImpl(
                alertRepository,
                transactionRepository,
                budgetRepository,
                List.of(strategy1),
                sseEmitterService
        );

        LocalDate date = LocalDate.of(2026, 3, 15);
        service.reEvaluateBudgetAlerts(10L, 20L, date);

        verify(alertRepository).deleteBudgetAlerts(
                eq(10L),
                eq(20L),
                eq(LocalDate.of(2026, 3, 1).atStartOfDay()),
                eq(LocalDate.of(2026, 3, 31).atTime(23, 59, 59))
        );
    }

    @Test
    void pushAlertToUser_sendsMappedResponse() {
        AlertServiceImpl service = new AlertServiceImpl(
                alertRepository,
                transactionRepository,
                budgetRepository,
                List.of(strategy1),
                sseEmitterService
        );

        Alert alert = Alert.builder()
                .id(5L)
                .alertType(AlertType.BUDGET_THRESHOLD)
                .message("m")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .user(User.builder().id(10L).build())
                .category(Category.builder().id(2L).name("Food").build())
                .build();

        service.pushAlertToUser(10L, alert);

        verify(sseEmitterService).sendAlert(eq(10L), responseCaptor.capture());
        assertThat(responseCaptor.getValue().getId()).isEqualTo(5L);
        assertThat(responseCaptor.getValue().getCategoryName()).isEqualTo("Food");
    }
}

