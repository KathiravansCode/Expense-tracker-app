package com.expensetrackaer.app.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor

public class SummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;

   public SummaryResponse(BigDecimal totalIncome,BigDecimal totalExpense){
       this.totalIncome=totalIncome;
       this.totalExpense=totalExpense;
       this.balance=totalIncome.subtract(totalExpense);
   }

}
