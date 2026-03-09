package com.expensetrackaer.app.controller;

import com.expensetrackaer.app.entity.dto.ApiResponse;
import com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse;
import com.expensetrackaer.app.entity.dto.SpendingTrendResponse;
import com.expensetrackaer.app.entity.dto.SummaryResponse;
import com.expensetrackaer.app.entity.model.Month;
import com.expensetrackaer.app.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService){
        this.analyticsService=analyticsService;
    }

    @GetMapping("/summary")
    public ApiResponse getSummary(
            @RequestParam(required = false) Month month,
            @RequestParam(required = false) Integer year
    ) {

        SummaryResponse summary =
                analyticsService.getSummary(month, year);

        return new ApiResponse(true, "Summary fetched successfully", summary);
    }

    @GetMapping("/category-breakdown")
    public ApiResponse getCategoryBreakdown(
            @RequestParam(required = false) Month month,
            @RequestParam(required = false) Integer year
    ) {

        List<CategoryBreakdownResponse> breakdown =
                analyticsService.getCategoryBreakdown(month, year);

        return new ApiResponse(true, "Category breakdown fetched", breakdown);
    }

    @GetMapping("/spending-trend")
    public ApiResponse getSpendingTrend(
            @RequestParam(required = false) Month month,
            @RequestParam(required = false) Integer year
    ) {

        List<SpendingTrendResponse> trend =
                analyticsService.getSpendingTrend(month, year);

        return new ApiResponse(true, "Spending trend fetched", trend);
    }

}
