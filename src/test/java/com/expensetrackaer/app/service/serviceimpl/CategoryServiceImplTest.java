package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.CategoryResponse;
import com.expensetrackaer.app.entity.dto.CreateCategoryRequest;
import com.expensetrackaer.app.entity.model.Category;
import com.expensetrackaer.app.entity.model.User;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.CategoryRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private CategoryServiceImpl categoryService;

    @Captor private ArgumentCaptor<Category> categoryCaptor;

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void createCategory_duplicateInUserCustom_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Food");

        when(categoryRepository.existsByNameIgnoreCaseAndUserId("Food", 1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("already have a custom category");
    }

    @Test
    void createCategory_duplicateInGlobalDefaults_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Food");

        when(categoryRepository.existsByNameIgnoreCaseAndUserId("Food", 1L)).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndUserIsNull("Food")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("default category");
    }

    @Test
    void createCategory_userNotFound_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("NewCat");

        when(categoryRepository.existsByNameIgnoreCaseAndUserId("NewCat", 1L)).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndUserIsNull("NewCat")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createCategory_success_savesWithUser() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("NewCat");

        User user = User.builder().id(1L).email("a@example.com").name("A").password("x").build();
        when(categoryRepository.existsByNameIgnoreCaseAndUserId("NewCat", 1L)).thenReturn(false);
        when(categoryRepository.existsByNameIgnoreCaseAndUserIsNull("NewCat")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CategoryResponse response = categoryService.createCategory(request);

        verify(categoryRepository).save(categoryCaptor.capture());
        assertThat(categoryCaptor.getValue().getUser().getId()).isEqualTo(1L);
        assertThat(categoryCaptor.getValue().getName()).isEqualTo("NewCat");
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("NewCat");
    }

    @Test
    void getCategories_returnsAccessibleCategories() {
        SecurityTestUtils.setAuthenticatedUser(2L, "b@example.com");

        when(categoryRepository.findAllAccessibleByUser(2L)).thenReturn(List.of(
                Category.builder().id(1L).name("Food").user(null).build(),
                Category.builder().id(2L).name("Custom").user(User.builder().id(2L).build()).build()
        ));

        List<CategoryResponse> responses = categoryService.getCategories();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Food");
        assertThat(responses.get(1).getName()).isEqualTo("Custom");
    }

    @Test
    void updateCategory_whenDefaultOrNotOwned_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("NewName");

        when(categoryRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Default categories cannot be edited");
    }

    @Test
    void deleteCategory_whenHasTransactions_throws() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        Category owned = Category.builder().id(5L).name("Owned").user(User.builder().id(1L).build()).build();
        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(owned));
        when(transactionRepository.existsByCategory_Id(5L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(5L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("cannot be deleted");
    }

    @Test
    void deleteCategory_success_deletes() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");
        Category owned = Category.builder().id(5L).name("Owned").user(User.builder().id(1L).build()).build();
        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(owned));
        when(transactionRepository.existsByCategory_Id(5L)).thenReturn(false);

        categoryService.deleteCategory(5L);

        verify(categoryRepository).delete(owned);
    }
}
