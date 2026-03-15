package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.CreateTransactionRequest;
import com.expensetrackaer.app.entity.dto.TransactionResponse;
import com.expensetrackaer.app.entity.model.*;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.CategoryRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.service.AlertService;
import com.expensetrackaer.app.service.TransactionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AlertService alertService;
    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  CategoryRepository categoryRepository,
                                  UserRepository userRepository,AlertService alertService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.alertService=alertService;
    }

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request) {


        Long userId = 1L; // temporary until authentication

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessValidationException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessValidationException("Category not found"));

        Transaction transaction = new Transaction();

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setUser(user);
        transaction.setCategory(category);

        Transaction saved = transactionRepository.save(transaction);
        alertService.checkAlerts(saved);

        return mapToResponse(saved);


    }

    @Override
    public TransactionResponse updateTransaction(Long id, CreateTransactionRequest request) {
        Long userId = 1L;

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new BusinessValidationException("Transaction not found"));

        Category category = categoryRepository
                .findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() ->
                        new BusinessValidationException("Category not found or not owned by user"));

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setPaymentMode(request.getPaymentMode());
        transaction.setCategory(category);

        Transaction updatedTransaction = transactionRepository.save(transaction);

        return mapToResponse(updatedTransaction);

    }

    @Override
    public void deleteTransaction(Long id) {
        Long userId = 1L;

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new BusinessValidationException("Transaction not found"));

        transactionRepository.delete(transaction);

    }

    @Override
    public Page<TransactionResponse> getTransactions(Integer month, Integer year, TransactionType type, Pageable pageable) {

        Long userId = 1L;

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (month != null && year != null) {

            startDate = LocalDate.of(year, month, 1);

            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        Page<Transaction> transactions = transactionRepository
                .findAllByFilters(userId, type, startDate, endDate, pageable);

        return transactions.map(this::mapToResponse);
    }

    @Override
    public TransactionResponse getTransactionById(Long id) {

       Transaction transaction=transactionRepository.findById(id).
               orElseThrow(()->new ResourceNotFoundException("Transaction Not found for the Given Id"));

       return mapToResponse(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {

        TransactionResponse response = new TransactionResponse();

        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getTransactionDate());

        response.setCategoryId(transaction.getCategory().getId());
        response.setCategoryName(transaction.getCategory().getName());

        return response;
    }
}
