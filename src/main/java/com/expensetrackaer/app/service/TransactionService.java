package com.expensetrackaer.app.service;

import com.expensetrackaer.app.entity.dto.CreateTransactionRequest;
import com.expensetrackaer.app.entity.dto.TransactionResponse;
import com.expensetrackaer.app.entity.model.Month;
import com.expensetrackaer.app.entity.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponse createTransaction(CreateTransactionRequest request);

    TransactionResponse updateTransaction(Long id, CreateTransactionRequest request);

    void deleteTransaction(Long id);

    Page<TransactionResponse> getTransactions(
            Integer month,
            Integer year,
            TransactionType type,
            Pageable pageable
    );

    TransactionResponse getTransactionById(Long id);
}
