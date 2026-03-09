package com.expensetrackaer.app.entity.dto;

import com.expensetrackaer.app.entity.model.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;

    private BigDecimal amount;

    private String description;

    private String paymentMode;

    private Boolean isUnusual;

    private TransactionType transactionType;

    private Long categoryId;

    private String categoryName;

    private LocalDate transactionDate;
}
