package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.model.Category;
import com.expensetrackaer.app.entity.model.Transaction;
import com.expensetrackaer.app.entity.model.TransactionType;
import com.expensetrackaer.app.entity.model.User;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.testutil.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private ExportServiceImpl exportService;

    @Captor private ArgumentCaptor<Long> userIdCaptor;
    @Captor private ArgumentCaptor<LocalDate> startCaptor;
    @Captor private ArgumentCaptor<LocalDate> endCaptor;

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void exportTransactions_usesCurrentUserId_andBuildsCsv() throws Exception {
        SecurityTestUtils.setAuthenticatedUser(42L, "a@example.com");

        Transaction t = Transaction.builder()
                .transactionDate(LocalDate.of(2026, 3, 1))
                .category(Category.builder().id(1L).name("Food").build())
                .transactionType(TransactionType.EXPENSE)
                .amount(new BigDecimal("12.34"))
                .description("hello, world")
                .user(User.builder().id(42L).build())
                .build();

        when(transactionRepository.findTransactionsForExport(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t));

        Resource resource = exportService.exportTransactions(999L, 3, 2026);

        verify(transactionRepository).findTransactionsForExport(
                userIdCaptor.capture(),
                startCaptor.capture(),
                endCaptor.capture()
        );

        assertThat(userIdCaptor.getValue()).isEqualTo(42L);
        assertThat(startCaptor.getValue()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(endCaptor.getValue()).isEqualTo(LocalDate.of(2026, 3, 31));

        String csv = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(csv).contains("Date,Category,Type,Amount,Description");
        assertThat(csv).contains("2026-03-01,Food,EXPENSE,12.34,hello  world");
    }
}

