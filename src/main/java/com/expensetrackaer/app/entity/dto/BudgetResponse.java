package com.expensetrackaer.app.entity.dto;

import com.expensetrackaer.app.entity.model.Month;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {

    private Long id;

    private BigDecimal limitAmount;

    private Month month;

    private Integer year;

    private Long categoryId;

    private String categoryName;
}
