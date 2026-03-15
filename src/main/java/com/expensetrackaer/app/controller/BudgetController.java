package com.expensetrackaer.app.controller;

import com.expensetrackaer.app.entity.dto.ApiResponse;
import com.expensetrackaer.app.entity.dto.BudgetResponse;
import com.expensetrackaer.app.entity.dto.CreateBudgetRequest;
import com.expensetrackaer.app.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {

        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createBudget(@Valid @RequestBody CreateBudgetRequest request) {

        BudgetResponse response = budgetService.createBudget(request);

        ApiResponse apiResponse=new ApiResponse(true, "Budget created successfully", response);

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse> getCurrentBudget() {

        BudgetResponse response = budgetService.getCurrentBudget();

       ApiResponse apiResponse=new ApiResponse(true, "Current budget fetched", response);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUserBudgets() {

        List<BudgetResponse> budgets = budgetService.getBudgets();

        ApiResponse apiResponse=new ApiResponse(true, "Budgets fetched", budgets);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);

    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody CreateBudgetRequest request) {

        BudgetResponse response = budgetService.updateBudget(id, request);

        return ResponseEntity.ok(
                new ApiResponse(true, "Budget updated successfully", response)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBudget(@PathVariable Long id) {

        budgetService.deleteBudget(id);

        return ResponseEntity.ok(
                new ApiResponse(true, "Budget deleted successfully", null)
        );
    }
}
