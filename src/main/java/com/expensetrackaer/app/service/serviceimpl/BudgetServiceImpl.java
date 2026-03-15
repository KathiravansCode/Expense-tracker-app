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
    public BudgetServiceImpl(BudgetRepository budgetRepository,CategoryRepository categoryRepository,UserRepository userRepository){
        this.budgetRepository=budgetRepository;
        this.categoryRepository=categoryRepository;
        this.userRepository=userRepository;
    }

    private Long getCurrentUserId() {
        return 1L; // temporary until JWT
    }
    @Override
    public BudgetResponse createBudget(CreateBudgetRequest budgetRequest) {

        Long userId=getCurrentUserId();

        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                userId,
                budgetRequest.getCategoryId(),
                Month.fromValue(budgetRequest.getMonth()),
                budgetRequest.getYear())) {

            throw new BusinessValidationException(
                    "Budget already exists for this category and month"
            );
        }

       User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Category category = categoryRepository.findById(budgetRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Budget budget = new Budget();
        budget.setLimitAmount(budgetRequest.getLimitAmount());
        budget.setMonth(Month.fromValue(budgetRequest.getMonth()));
        budget.setYear(budgetRequest.getYear());
        budget.setUser(user);
        budget.setCategory(category);

        Budget saved = budgetRepository.save(budget);

        return mapToBudgetResponse(saved);
    }

    @Override
    public List<BudgetResponse> getBudgets() {
        Long userId = 1L;

        return budgetRepository.findByUserId(userId)
                .stream()
                .map(this::mapToBudgetResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BudgetResponse getCurrentBudget() {
        Long userId = 1L;

        LocalDate now = LocalDate.now();

        Month currentMonth = Month.values()[now.getMonthValue() - 1];

        Integer year = now.getYear();

        Budget budget = budgetRepository
                .findByUserIdAndMonthAndYear(userId, currentMonth, year)
                .orElseThrow(() -> new BusinessValidationException("Budget not found for current month"));



        return mapToBudgetResponse(budget);
    }

    @Override
    public BudgetResponse updateBudget(Long id,CreateBudgetRequest budgetRequest) {
        Long userId = getCurrentUserId();

        Budget budget = budgetRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        Category category = categoryRepository.findById(budgetRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        budget.setLimitAmount(budgetRequest.getLimitAmount());
        budget.setMonth(Month.fromValue(budgetRequest.getMonth()));
        budget.setYear(budgetRequest.getYear());
        budget.setCategory(category);

        Budget updated = budgetRepository.save(budget);

        return mapToBudgetResponse(updated);
    }

    @Override
    public BudgetResponse getBudget(Long id) {
      Budget budget=budgetRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("No budget found"));

      return mapToBudgetResponse(budget);
    }

    @Override
    public void deleteBudget(Long id) {

        Long userId = getCurrentUserId();

        Budget budget = budgetRepository
                .findByIdAndUserId(id, userId)
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

//onth(Month.valueOf(budget.getMonth().name()))


}