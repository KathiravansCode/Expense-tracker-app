package com.expensetrackaer.app.strategy;


import com.expensetrackaer.app.entity.model.Transaction;

public interface AlertStrategy {

    void check(Transaction transaction);

}
