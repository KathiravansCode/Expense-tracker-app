package com.expensetrackaer.app.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBreakdownResponse {

    private String categoryName;

    private BigDecimal totalAmount;
}
