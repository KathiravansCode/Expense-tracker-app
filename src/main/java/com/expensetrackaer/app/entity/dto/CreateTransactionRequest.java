package com.expensetrackaer.app.entity.dto;


import com.expensetrackaer.app.entity.model.PaymentMode;
import com.expensetrackaer.app.entity.model.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransactionRequest {
    @NotNull(message="Transaction without amount is not possible")
    @Positive(message="Negative value of amount is not accepted")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Size(max=500,message="Description cannot be more than 500characters")
    private String description;


    @NotNull(message="Transaction  Date cannot be null")
    @PastOrPresent(message="Date cannot be in the future")
    private LocalDate transactionDate;

    @NotNull(message="Every transaction must belong to a category")
    private Long categoryId;

    @NotNull(message="Income/Expense should be selected to perform transaction ")
    private TransactionType transactionType;

    private PaymentMode paymentMode;

}
