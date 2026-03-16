package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse;
import com.expensetrackaer.app.entity.dto.SpendingTrendResponse;
import com.expensetrackaer.app.entity.dto.SummaryResponse;
import com.expensetrackaer.app.repository.AnalyticsRepository;
import com.expensetrackaer.app.security.SecurityUtils;
import com.expensetrackaer.app.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    @Autowired
    public AnalyticsServiceImpl(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    // ✅ Replaced hardcoded return 1L with real user from JWT
    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    private LocalDate[] getDateRange(Integer month, Integer year) {
        if (month == null || year == null) {
            return new LocalDate[]{null, null};
        }
        LocalDate startDate = LocalDate.of(year, month, 1);
        return new LocalDate[]{startDate, startDate.withDayOfMonth(startDate.lengthOfMonth())};
    }

    @Override
    public SummaryResponse getSummary(Integer month, Integer year) {

        Long userId = getCurrentUserId();
        LocalDate[] range = getDateRange(month, year);

        List<Object[]> results = analyticsRepository.getSummary(userId, range[0], range[1]);
        Object[] row = results.getFirst();

        return new SummaryResponse(
                new BigDecimal(row[0].toString()),
                new BigDecimal(row[1].toString())
        );
    }

    @Override
    public List<CategoryBreakdownResponse> getCategoryBreakdown(Integer month, Integer year) {
        Long userId = getCurrentUserId();
        LocalDate[] range = getDateRange(month, year);
        return analyticsRepository.getCategoryBreakdown(userId, range[0], range[1]);
    }

    @Override
    public List<SpendingTrendResponse> getSpendingTrend(Integer month, Integer year) {
        Long userId = getCurrentUserId();
        LocalDate[] range = getDateRange(month, year);
        return analyticsRepository.getDailySpendingTrend(userId, range[0], range[1]);
    }
}