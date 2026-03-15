package com.expensetrackaer.app.entity.dto;

import com.expensetrackaer.app.entity.model.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportTransactionRow {

    private LocalDate localDate;

    private String categoryName;

    private TransactionType transactionType;

    private BigDecimal amount;

    private String description;
}
