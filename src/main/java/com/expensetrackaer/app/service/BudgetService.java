package com.expensetrackaer.app.service;

import com.expensetrackaer.app.entity.dto.BudgetResponse;
import com.expensetrackaer.app.entity.dto.CreateBudgetRequest;

import java.util.List;

public interface BudgetService {

BudgetResponse createBudget(CreateBudgetRequest budgetRequest);

List<BudgetResponse> getBudgets();

List<BudgetResponse> getCurrentBudget();

BudgetResponse updateBudget(Long id,CreateBudgetRequest budgetRequest);

BudgetResponse getBudget(Long id);

 void deleteBudget(Long id);

}
