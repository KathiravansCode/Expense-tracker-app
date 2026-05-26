package com.expensetrackaer.app.integration;

import com.expensetrackaer.app.entity.model.*;
import com.expensetrackaer.app.repository.CategoryRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("it")
class TransactionRepositoryPostgresIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("expense_it")
            .withUsername("it_user")
            .withPassword("it_pass");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;

    @Test
    void getMonthlyExpenseTotal_sumsOnlyExpensesInRange() {
        User user = userRepository.save(User.builder()
                .name("A")
                .email("a@example.com")
                .password("x")
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Food")
                .user(user)
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .category(category)
                .transactionType(TransactionType.EXPENSE)
                .amount(new BigDecimal("10.00"))
                .isUnusual(false)
                .paymentMode(PaymentMode.CASH)
                .transactionDate(LocalDate.of(2026, 3, 10))
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .category(category)
                .transactionType(TransactionType.INCOME)
                .amount(new BigDecimal("99.00"))
                .isUnusual(false)
                .paymentMode(PaymentMode.CASH)
                .transactionDate(LocalDate.of(2026, 3, 11))
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .category(category)
                .transactionType(TransactionType.EXPENSE)
                .amount(new BigDecimal("5.00"))
                .isUnusual(false)
                .paymentMode(PaymentMode.CASH)
                .transactionDate(LocalDate.of(2026, 4, 1))
                .build());

        BigDecimal total = transactionRepository.getMonthlyExpenseTotal(
                user.getId(),
                category.getId(),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );

        assertThat(total).isEqualByComparingTo("10.00");
    }

    @Test
    void getAverageExpense_returnsAverageAcrossExpenses() {
        User user = userRepository.save(User.builder()
                .name("B")
                .email("b@example.com")
                .password("x")
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Misc")
                .user(user)
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .category(category)
                .transactionType(TransactionType.EXPENSE)
                .amount(new BigDecimal("10.00"))
                .isUnusual(false)
                .paymentMode(PaymentMode.CASH)
                .transactionDate(LocalDate.of(2026, 3, 1))
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .category(category)
                .transactionType(TransactionType.EXPENSE)
                .amount(new BigDecimal("30.00"))
                .isUnusual(false)
                .paymentMode(PaymentMode.CASH)
                .transactionDate(LocalDate.of(2026, 3, 2))
                .build());

        BigDecimal avg = transactionRepository.getAverageExpense(user.getId());

        assertThat(avg).isNotNull();
        assertThat(avg).isEqualByComparingTo("20.00");
    }
}
