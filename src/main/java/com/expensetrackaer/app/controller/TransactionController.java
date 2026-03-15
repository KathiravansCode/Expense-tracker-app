package com.expensetrackaer.app.controller;

import com.expensetrackaer.app.entity.dto.ApiResponse;
import com.expensetrackaer.app.entity.dto.CreateTransactionRequest;
import com.expensetrackaer.app.entity.dto.TransactionResponse;
import com.expensetrackaer.app.entity.model.Month;
import com.expensetrackaer.app.entity.model.TransactionType;
import com.expensetrackaer.app.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService){

        this.transactionService=transactionService;
    }

    @PostMapping
    public ApiResponse createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {

        TransactionResponse response =
                transactionService.createTransaction(request);

        return new ApiResponse(true, "Transaction created successfully", response);
    }

    @GetMapping
    public ApiResponse getTransactions(

            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) TransactionType type,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());

        Page<TransactionResponse> transactions =
                transactionService.getTransactions(month, year, type, pageable);

        return new ApiResponse(true, "Transactions fetched", transactions);
    }

    @GetMapping("/{id}")
    public ApiResponse getTransactionById(@PathVariable Long id) {

        TransactionResponse transaction =
                transactionService.getTransactionById(id);

        return new ApiResponse(true, "Transaction fetched successfully", transaction);
    }

    @PutMapping("/{id}")
    public ApiResponse updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody CreateTransactionRequest request) {

        TransactionResponse transaction =
                transactionService.updateTransaction(id, request);

        return new ApiResponse(true, "Transaction updated successfully", transaction);
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteTransaction(@PathVariable Long id) {

        transactionService.deleteTransaction(id);

        return new ApiResponse(true, "Transaction deleted", null);
    }

}
