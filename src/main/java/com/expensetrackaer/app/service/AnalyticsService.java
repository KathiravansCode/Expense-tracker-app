package com.expensetrackaer.app.service;

import com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse;
import com.expensetrackaer.app.entity.dto.SpendingTrendResponse;
import com.expensetrackaer.app.entity.dto.SummaryResponse;
import com.expensetrackaer.app.entity.model.Month;

import java.util.List;


public  interface AnalyticsService {
    SummaryResponse getSummary(Integer month, Integer year);

    List<CategoryBreakdownResponse> getCategoryBreakdown(Integer month, Integer year);

    List<SpendingTrendResponse> getSpendingTrend(Integer month, Integer year);
}


