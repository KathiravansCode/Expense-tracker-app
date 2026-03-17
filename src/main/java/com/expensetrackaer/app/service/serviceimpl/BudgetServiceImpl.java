package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.BudgetResponse;
import com.expensetrackaer.app.entity.dto.CreateBudgetRequest;
import com.expensetrackaer.app.entity.model.*;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.BudgetRepository;
import com.expensetrackaer.app.repository.CategoryRepository;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.security.SecurityUtils;
import com.expensetrackaer.app.service.BudgetService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             CategoryRepository categoryRepository,
                             UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // ✅ Replaced hardcoded return 1L with real user from JWT
    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    @Override
    public BudgetResponse createBudget(CreateBudgetRequest request) {

        Long userId = getCurrentUserId();

        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                userId,
                request.getCategoryId(),
                Month.fromValue(request.getMonth()),
                request.getYear())) {
            throw new BusinessValidationException(
                    "Budget already exists for this category and month");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ findAccessibleCategory — allows global (user IS NULL) and user's own categories
        Category category = categoryRepository
                .findAccessibleCategory(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found or not accessible"));

        Budget budget = new Budget();
        budget.setLimitAmount(request.getLimitAmount());
        budget.setMonth(Month.fromValue(request.getMonth()));
        budget.setYear(request.getYear());
        budget.setUser(user);
        budget.setCategory(category);

        return mapToBudgetResponse(budgetRepository.save(budget));
    }

    @Override
    public List<BudgetResponse> getBudgets() {
        return budgetRepository.findByUserId(getCurrentUserId())
                .stream()
                .map(this::mapToBudgetResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetResponse> getCurrentBudget() {
        Long userId = getCurrentUserId();
        LocalDate now = LocalDate.now();
        Month currentMonth = Month.values()[now.getMonthValue() - 1];

        List<Budget> budgets = budgetRepository
                .findByUserIdAndMonthAndYear(userId, currentMonth, now.getYear());

        if (budgets.isEmpty()) {
            throw new BusinessValidationException("No budget found for the current month");
        }

        return budgets.stream()
                .map(this::mapToBudgetResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BudgetResponse updateBudget(Long id, CreateBudgetRequest request) {

        Long userId = getCurrentUserId();

        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        // ✅ findAccessibleCategory — allows global and user-owned categories
        Category category = categoryRepository
                .findAccessibleCategory(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found or not accessible"));

        budget.setLimitAmount(request.getLimitAmount());
        budget.setMonth(Month.fromValue(request.getMonth()));
        budget.setYear(request.getYear());
        budget.setCategory(category);

        return mapToBudgetResponse(budgetRepository.save(budget));
    }

    @Override
    public BudgetResponse getBudget(Long id) {
        Long userId = getCurrentUserId();
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        return mapToBudgetResponse(budget);
    }

    @Override
    public void deleteBudget(Long id) {
        Long userId = getCurrentUserId();
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budgetRepository.delete(budget);
    }

    private BudgetResponse mapToBudgetResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .limitAmount(budget.getLimitAmount())
                .month(budget.getMonth().getValue())
                .year(budget.getYear())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .build();
    }
}