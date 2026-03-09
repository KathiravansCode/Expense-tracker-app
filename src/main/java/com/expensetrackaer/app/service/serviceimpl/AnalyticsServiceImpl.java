package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse;
import com.expensetrackaer.app.entity.dto.SpendingTrendResponse;
import com.expensetrackaer.app.entity.dto.SummaryResponse;
import com.expensetrackaer.app.entity.model.Month;
import com.expensetrackaer.app.repository.AnalyticsRepository;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;


    @Autowired
    public AnalyticsServiceImpl(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;

    }

    private Long getCurrentUserId() {

        return 1L; // later replaced with JWT user
    }

    private LocalDate[] getDateRange(Month month, Integer year) {

        if (month == null || year == null) {
            return new LocalDate[]{null, null};
        }

        LocalDate startDate = LocalDate.of(year, month.getValue(), 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return new LocalDate[]{startDate, endDate};
    }

    @Override
    public SummaryResponse getSummary(Month month, Integer year) {
        Long userId = getCurrentUserId();

        LocalDate[] range = getDateRange(month, year);

        Object[] result = analyticsRepository
                .getSummary(userId, range[0], range[1]);

        BigDecimal income = (BigDecimal) result[0];
        BigDecimal expense = (BigDecimal) result[1];

        return new SummaryResponse(income, expense);
    }

    @Override
    public List<CategoryBreakdownResponse> getCategoryBreakdown(Month month, Integer year) {
        Long userId = getCurrentUserId();

        LocalDate[] range = getDateRange(month, year);

        return analyticsRepository.getCategoryBreakdown(
                userId,
                range[0],
                range[1]
        );
    }

    @Override
    public List<SpendingTrendResponse> getSpendingTrend(Month month, Integer year) {
        Long userId = getCurrentUserId();

        LocalDate[] range = getDateRange(month, year);

        return analyticsRepository.getDailySpendingTrend(
                userId,
                range[0],
                range[1]
        );

    }
}
