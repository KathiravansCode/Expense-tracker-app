package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.CreateTransactionRequest;
import com.expensetrackaer.app.entity.dto.TransactionResponse;
import com.expensetrackaer.app.entity.model.*;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.CategoryRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.service.AlertService;
import com.expensetrackaer.app.testutil.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private AlertService alertService;

    @InjectMocks private TransactionServiceImpl transactionService;

    @Captor private ArgumentCaptor<Transaction> transactionCaptor;
    @Captor private ArgumentCaptor<Specification<Transaction>> specCaptor;

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void createTransaction_userNotFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .amount(new BigDecimal("10.00"))
                .categoryId(2L)
                .transactionDate(LocalDate.now())
                .transactionType(TransactionType.EXPENSE)
                .paymentMode(PaymentMode.CASH)
                .description("d")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createTransaction_categoryNotAccessible_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .amount(new BigDecimal("10.00"))
                .categoryId(2L)
                .transactionDate(LocalDate.now())
                .transactionType(TransactionType.EXPENSE)
                .paymentMode(PaymentMode.CASH)
                .description("d")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(categoryRepository.findAccessibleCategory(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void createTransaction_success_savesAndTriggersAlerts() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        LocalDate date = LocalDate.of(2026, 3, 1);

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .amount(new BigDecimal("10.00"))
                .categoryId(2L)
                .transactionDate(date)
                .transactionType(TransactionType.EXPENSE)
                .paymentMode(PaymentMode.CASH)
                .description("d")
                .build();

        User user = User.builder().id(1L).name("A").email("a@example.com").password("x").build();
        Category category = Category.builder().id(2L).name("Food").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findAccessibleCategory(2L, 1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        TransactionResponse response = transactionService.createTransaction(request);

        verify(transactionRepository).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getUser().getId()).isEqualTo(1L);
        assertThat(transactionCaptor.getValue().getCategory().getId()).isEqualTo(2L);
        verify(alertService).checkAlerts(any(Transaction.class));

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCategoryName()).isEqualTo("Food");
        assertThat(response.getPaymentMode()).isEqualTo("CASH");
    }

    @Test
    void updateTransaction_notFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .amount(new BigDecimal("10.00"))
                .categoryId(2L)
                .transactionDate(LocalDate.now())
                .transactionType(TransactionType.EXPENSE)
                .build();

        when(transactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(99L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    void updateTransaction_success_reEvaluatesBudgetAlerts() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        LocalDate date = LocalDate.of(2026, 3, 5);

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .amount(new BigDecimal("20.00"))
                .categoryId(2L)
                .transactionDate(date)
                .transactionType(TransactionType.EXPENSE)
                .paymentMode(PaymentMode.UPI)
                .description("updated")
                .build();

        Transaction existing = Transaction.builder()
                .id(9L)
                .amount(new BigDecimal("1.00"))
                .transactionDate(date)
                .transactionType(TransactionType.EXPENSE)
                .paymentMode(PaymentMode.CASH)
                .user(User.builder().id(1L).build())
                .category(Category.builder().id(2L).name("Food").build())
                .build();

        when(transactionRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findAccessibleCategory(2L, 1L))
                .thenReturn(Optional.of(Category.builder().id(2L).name("Food").build()));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transactionService.updateTransaction(9L, request);

        verify(alertService).reEvaluateBudgetAlerts(1L, 2L, date);
        verify(transactionRepository).save(existing);
        assertThat(existing.getPaymentMode()).isEqualTo(PaymentMode.UPI);
        assertThat(existing.getAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void deleteTransaction_notFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        when(transactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    void deleteTransaction_success_deletesAndReEvaluates() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        LocalDate date = LocalDate.of(2026, 3, 10);
        Transaction existing = Transaction.builder()
                .id(9L)
                .transactionDate(date)
                .transactionType(TransactionType.EXPENSE)
                .amount(new BigDecimal("5.00"))
                .user(User.builder().id(1L).build())
                .category(Category.builder().id(2L).name("Food").build())
                .build();

        when(transactionRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.of(existing));

        transactionService.deleteTransaction(9L);

        verify(transactionRepository).delete(existing);
        verify(alertService).reEvaluateBudgetAlerts(1L, 2L, date);
    }

    @Test
    void getTransactionById_notFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        when(transactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    void getTransactions_buildsSpecificationAndMapsResponse() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("transactionDate").descending());
        Transaction tx = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("12.34"))
                .description("d")
                .transactionDate(LocalDate.of(2026, 3, 2))
                .transactionType(TransactionType.EXPENSE)
                .paymentMode(PaymentMode.CASH)
                .isUnusual(false)
                .user(User.builder().id(1L).build())
                .category(Category.builder().id(2L).name("Food").build())
                .build();

        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(java.util.List.of(tx), pageable, 1));

        Page<TransactionResponse> page = transactionService.getTransactions(3, 2026, TransactionType.EXPENSE, pageable);

        verify(transactionRepository).findAll(specCaptor.capture(), eq(pageable));
        assertThat(specCaptor.getValue()).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getCategoryName()).isEqualTo("Food");
    }
}
