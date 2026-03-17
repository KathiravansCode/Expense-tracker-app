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

    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    // ✅ Always returns actual dates — never null
    // Previously returned {null, null} when month/year not provided
    // which caused PostgreSQL "could not determine data type" error
    // Now defaults to full current year when no month/year is given
    private LocalDate[] getDateRange(Integer month, Integer year) {

        // If year not provided, default to current year
        int resolvedYear = (year != null) ? year : LocalDate.now().getYear();

        if (month != null) {
            // Specific month requested — return just that month's range
            LocalDate startDate = LocalDate.of(resolvedYear, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            return new LocalDate[]{startDate, endDate};
        } else {
            // No month provided — return full year range
            LocalDate startDate = LocalDate.of(resolvedYear, 1, 1);
            LocalDate endDate = LocalDate.of(resolvedYear, 12, 31);
            return new LocalDate[]{startDate, endDate};
        }
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