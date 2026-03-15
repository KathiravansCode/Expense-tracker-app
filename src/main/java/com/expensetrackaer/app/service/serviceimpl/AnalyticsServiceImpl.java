package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse;
import com.expensetrackaer.app.entity.dto.SpendingTrendResponse;
import com.expensetrackaer.app.entity.dto.SummaryResponse;
import com.expensetrackaer.app.entity.model.Month;
import com.expensetrackaer.app.repository.AnalyticsRepository;
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
    public SummaryResponse getSummary(Integer month, Integer year) {
        Long userId = getCurrentUserId();

        LocalDate[] range = getDateRange(Month.fromValue(month), year);

        List<Object[]> results = analyticsRepository
                .getSummary(userId, range[0], range[1]);
        Object[] summary=results.getFirst();
        BigDecimal income = new BigDecimal(summary[0].toString());
        BigDecimal expense = new BigDecimal(summary[1].toString());

        return new SummaryResponse(income, expense);
    }

    @Override
    public List<CategoryBreakdownResponse> getCategoryBreakdown(Integer month, Integer year) {
        Long userId = getCurrentUserId();

        LocalDate[] range = getDateRange(Month.fromValue(month), year);

        return analyticsRepository.getCategoryBreakdown(
                userId,
                range[0],
                range[1]
        );
    }

    @Override
    public List<SpendingTrendResponse> getSpendingTrend(Integer month, Integer year) {
        Long userId = getCurrentUserId();

        LocalDate[] range = getDateRange(Month.fromValue(month), year);

        return analyticsRepository.getDailySpendingTrend(
                userId,
                range[0],
                range[1]
        );

    }
}
