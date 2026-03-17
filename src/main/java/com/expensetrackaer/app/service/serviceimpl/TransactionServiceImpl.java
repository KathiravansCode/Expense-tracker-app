package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.CreateTransactionRequest;
import com.expensetrackaer.app.entity.dto.TransactionResponse;
import com.expensetrackaer.app.entity.model.*;
import com.expensetrackaer.app.exception.BusinessValidationException;
import com.expensetrackaer.app.exception.ResourceNotFoundException;
import com.expensetrackaer.app.repository.CategoryRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.repository.TransactionSpecification;
import com.expensetrackaer.app.repository.UserRepository;
import com.expensetrackaer.app.security.SecurityUtils;
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
                                  UserRepository userRepository,
                                  AlertService alertService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.alertService = alertService;
    }

    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request) {

        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessValidationException("User not found"));

        Category category = categoryRepository
                .findAccessibleCategory(request.getCategoryId(), userId)
                .orElseThrow(() -> new BusinessValidationException(
                        "Category not found or not accessible"));

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setPaymentMode(request.getPaymentMode());
        transaction.setUser(user);
        transaction.setCategory(category);

        Transaction saved = transactionRepository.save(transaction);

        alertService.checkAlerts(saved);

        return mapToResponse(saved);
    }

    @Override
    public TransactionResponse updateTransaction(Long id, CreateTransactionRequest request) {

        Long userId = getCurrentUserId();

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessValidationException("Transaction not found"));

        Category category = categoryRepository
                .findAccessibleCategory(request.getCategoryId(), userId)
                .orElseThrow(() -> new BusinessValidationException(
                        "Category not found or not accessible"));

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setPaymentMode(request.getPaymentMode());
        transaction.setCategory(category);

        alertService.reEvaluateBudgetAlerts(
                userId,
                category.getId(),
                request.getTransactionDate()
        );

        Transaction updated = transactionRepository.save(transaction);

        return mapToResponse(updated);
    }

    @Override
    public void deleteTransaction(Long id) {

        Long userId = getCurrentUserId();

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessValidationException("Transaction not found"));

        Long categoryId = transaction.getCategory().getId();
        LocalDate date = transaction.getTransactionDate();

        transactionRepository.delete(transaction);

        alertService.reEvaluateBudgetAlerts(userId, categoryId, date);
    }

    @Override
    public Page<TransactionResponse> getTransactions(Integer month, Integer year,
                                                     TransactionType type, Pageable pageable) {
        Long userId = getCurrentUserId();

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (month != null && year != null) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        // ✅ Specification builds query dynamically — only adds predicates for non-null values
        // Fixes PostgreSQL "could not determine data type of parameter" error
        // that occurred with the old JPQL null check pattern (:type IS NULL OR ...)
        return transactionRepository
                .findAll(
                        TransactionSpecification.filterBy(userId, type, startDate, endDate),
                        pageable
                )
                .map(this::mapToResponse);
    }

    @Override
    public TransactionResponse getTransactionById(Long id) {

        Long userId = getCurrentUserId();

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return mapToResponse(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {

        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setTransactionType(transaction.getTransactionType());
        response.setIsUnusual(transaction.getIsUnusual());
        response.setPaymentMode(
                transaction.getPaymentMode() != null
                        ? transaction.getPaymentMode().name()
                        : null
        );
        response.setCategoryId(transaction.getCategory().getId());
        response.setCategoryName(transaction.getCategory().getName());

        return response;
    }
}