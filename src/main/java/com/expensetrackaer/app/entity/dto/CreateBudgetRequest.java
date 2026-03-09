package com.expensetrackaer.app.entity.dto;

import com.expensetrackaer.app.entity.model.Month;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBudgetRequest {
    @NotNull(message="Please set the limit for the budget")
    @Positive(message="Negative value of limit amount is not accepted")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal limitAmount;

    @NotNull
    private Month month;

    @NotNull
    @Positive
    private Integer year;

    @NotNull
    private Long categoryId;

}
