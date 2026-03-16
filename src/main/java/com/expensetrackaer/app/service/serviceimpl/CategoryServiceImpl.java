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
import com.expensetrackaer.app.security.SecurityUtils;
import com.expensetrackaer.app.service.CategoryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               TransactionRepository transactionRepository,
                               UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    // ── Create ────────────────────────────────────────────────────
    // Only creates user-specific custom categories.
    // Global categories are seeded by DatabaseSeeder — not created here.
    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        Long userId = getCurrentUserId();

        // Check duplicate among user's own custom categories
        if (categoryRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new BusinessValidationException(
                    "You already have a custom category with this name");
        }

        // Prevent creating a custom category with the same name as a global one
        // "Food" is already global — no point duplicating it per user
        if (categoryRepository.existsByNameAndUserIsNull(request.getName())) {
            throw new BusinessValidationException(
                    "'" + request.getName() + "' already exists as a default category");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = new Category();
        category.setName(request.getName());
        category.setUser(user);

        Category saved = categoryRepository.save(category);

        return new CategoryResponse(saved.getId(), saved.getName());
    }

    // ── Get All ───────────────────────────────────────────────────
    // Returns global categories + user's own custom categories combined
    @Override
    public List<CategoryResponse> getCategories() {

        Long userId = getCurrentUserId();

        // findAllAccessibleByUser fetches where user IS NULL OR user.id = userId
        return categoryRepository.findAllAccessibleByUser(userId)
                .stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    // ── Update ────────────────────────────────────────────────────
    // Only user's own custom categories can be updated.
    // findByIdAndUserId excludes global categories (user IS NULL)
    // so if the user tries to edit a global one, it throws not found.
    @Override
    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {

        Long userId = getCurrentUserId();

        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found. Default categories cannot be edited"));

        category.setName(request.getName());

        Category updated = categoryRepository.save(category);

        return new CategoryResponse(updated.getId(), updated.getName());
    }

    // ── Delete ────────────────────────────────────────────────────
    // Only user's own custom categories can be deleted.
    // findByIdAndUserId excludes global categories (user IS NULL)
    // so if the user tries to delete a global one, it throws not found.
    @Override
    public void deleteCategory(Long id) {

        Long userId = getCurrentUserId();

        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found. Default categories cannot be deleted"));

        if (transactionRepository.existsByCategory_Id(id)) {
            throw new BusinessValidationException(
                    "Category is associated with a transaction and cannot be deleted");
        }

        categoryRepository.delete(category);
    }
}