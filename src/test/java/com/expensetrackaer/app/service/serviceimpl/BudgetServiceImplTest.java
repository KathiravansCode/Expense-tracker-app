package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.BudgetResponse;
import com.expensetrackaer.app.entity.dto.CreateBudgetRequest;
import com.expensetrackaer.app.entity.model.Budget;
import com.expensetrackaer.app.entity.model.Category;
import com.expensetrackaer.app.entity.model.Month;
import com.expensetrackaer.app.entity.model.User;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.BudgetRepository;
import com.expensetrackaer.app.repository.CategoryRepository;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.testutil.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private BudgetServiceImpl budgetService;

    @Captor private ArgumentCaptor<Budget> budgetCaptor;

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void createBudget_whenBudgetAlreadyExists_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateBudgetRequest request = new CreateBudgetRequest(
                new BigDecimal("1000.00"), 3, 2026, 10L
        );

        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                1L, 10L, Month.MARCH, 2026
        )).thenReturn(true);

        assertThatThrownBy(() -> budgetService.createBudget(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Budget already exists");
    }

    @Test
    void createBudget_userNotFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateBudgetRequest request = new CreateBudgetRequest(
                new BigDecimal("1000.00"), 3, 2026, 10L
        );

        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                1L, 10L, Month.MARCH, 2026
        )).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.createBudget(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createBudget_categoryNotAccessible_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateBudgetRequest request = new CreateBudgetRequest(
                new BigDecimal("1000.00"), 3, 2026, 10L
        );

        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                1L, 10L, Month.MARCH, 2026
        )).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(categoryRepository.findAccessibleCategory(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.createBudget(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void createBudget_success_savesAndMapsResponse() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateBudgetRequest request = new CreateBudgetRequest(
                new BigDecimal("1000.00"), 3, 2026, 10L
        );

        User user = User.builder().id(1L).email("a@example.com").name("A").password("x").build();
        Category category = Category.builder().id(10L).name("Food").build();

        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                1L, 10L, Month.MARCH, 2026
        )).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findAccessibleCategory(10L, 1L)).thenReturn(Optional.of(category));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> {
            Budget b = inv.getArgument(0);
            b.setId(55L);
            return b;
        });

        BudgetResponse response = budgetService.createBudget(request);

        verify(budgetRepository).save(budgetCaptor.capture());
        assertThat(budgetCaptor.getValue().getUser().getId()).isEqualTo(1L);
        assertThat(budgetCaptor.getValue().getCategory().getId()).isEqualTo(10L);

        assertThat(response.getId()).isEqualTo(55L);
        assertThat(response.getMonth()).isEqualTo(3);
        assertThat(response.getYear()).isEqualTo(2026);
        assertThat(response.getCategoryName()).isEqualTo("Food");
    }

    @Test
    void getCurrentBudget_whenEmpty_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        LocalDate now = LocalDate.now();
        Month currentMonth = Month.values()[now.getMonthValue() - 1];

        when(budgetRepository.findByUserIdAndMonthAndYear(1L, currentMonth, now.getYear()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> budgetService.getCurrentBudget())
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("No budget found");
    }

    @Test
    void getBudgets_mapsResponses() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        Category category = Category.builder().id(10L).name("Food").build();
        Budget budget = Budget.builder()
                .id(1L)
                .limitAmount(new BigDecimal("100.00"))
                .month(Month.JANUARY)
                .year(2026)
                .category(category)
                .user(User.builder().id(1L).build())
                .build();

        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));

        List<BudgetResponse> responses = budgetService.getBudgets();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCategoryName()).isEqualTo("Food");
        assertThat(responses.get(0).getMonth()).isEqualTo(1);
    }

    @Test
    void updateBudget_notFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateBudgetRequest request = new CreateBudgetRequest(
                new BigDecimal("200.00"), 4, 2026, 10L
        );

        when(budgetRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.updateBudget(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget not found");
    }

    @Test
    void updateBudget_success_updatesFields() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateBudgetRequest request = new CreateBudgetRequest(
                new BigDecimal("200.00"), 4, 2026, 10L
        );

        Budget existing = Budget.builder()
                .id(9L)
                .limitAmount(new BigDecimal("100.00"))
                .month(Month.MARCH)
                .year(2026)
                .user(User.builder().id(1L).build())
                .category(Category.builder().id(10L).name("Old").build())
                .build();

        Category newCategory = Category.builder().id(10L).name("Food").build();

        when(budgetRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findAccessibleCategory(10L, 1L)).thenReturn(Optional.of(newCategory));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        BudgetResponse response = budgetService.updateBudget(9L, request);

        assertThat(existing.getLimitAmount()).isEqualByComparingTo("200.00");
        assertThat(existing.getMonth()).isEqualTo(Month.APRIL);
        assertThat(existing.getCategory().getName()).isEqualTo("Food");
        assertThat(response.getMonth()).isEqualTo(4);
    }

    @Test
    void getBudget_notFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        when(budgetRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getBudget(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteBudget_success_deletes() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        Budget existing = Budget.builder()
                .id(9L)
                .user(User.builder().id(1L).build())
                .category(Category.builder().id(10L).name("Food").build())
                .month(Month.MARCH)
                .year(2026)
                .limitAmount(new BigDecimal("100.00"))
                .build();

        when(budgetRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.of(existing));

        budgetService.deleteBudget(9L);

        verify(budgetRepository).delete(existing);
    }
}
