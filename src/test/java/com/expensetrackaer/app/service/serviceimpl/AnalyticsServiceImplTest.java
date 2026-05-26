package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.dto.CategoryBreakdownResponse;
import com.expensetrackaer.app.entity.dto.SpendingTrendResponse;
import com.expensetrackaer.app.entity.dto.SummaryResponse;
import com.expensetrackaer.app.repository.AnalyticsRepository;
import com.expensetrackaer.app.testutil.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock private AnalyticsRepository analyticsRepository;
    @InjectMocks private AnalyticsServiceImpl analyticsService;

    @Captor private ArgumentCaptor<LocalDate> startCaptor;
    @Captor private ArgumentCaptor<LocalDate> endCaptor;

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void getSummary_whenMonthAndYearNull_defaultsToCurrentYear() {
        SecurityTestUtils.setAuthenticatedUser(1L, "a@example.com");

        when(analyticsRepository.getSummary(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(java.util.Collections.singletonList(new Object[]{"100.00", "40.00"}));

        SummaryResponse response = analyticsService.getSummary(null, null);

        verify(analyticsRepository).getSummary(eq(1L), startCaptor.capture(), endCaptor.capture());
        int currentYear = LocalDate.now().getYear();
        assertThat(startCaptor.getValue()).isEqualTo(LocalDate.of(currentYear, 1, 1));
        assertThat(endCaptor.getValue()).isEqualTo(LocalDate.of(currentYear, 12, 31));
        assertThat(response.getBalance()).isEqualByComparingTo("60.00");
    }

    @Test
    void getCategoryBreakdown_monthProvided_usesMonthRange() {
        SecurityTestUtils.setAuthenticatedUser(2L, "b@example.com");

        when(analyticsRepository.getCategoryBreakdown(eq(2L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new CategoryBreakdownResponse("Food", java.math.BigDecimal.TEN)));

        List<CategoryBreakdownResponse> results = analyticsService.getCategoryBreakdown(3, 2026);

        verify(analyticsRepository).getCategoryBreakdown(eq(2L), startCaptor.capture(), endCaptor.capture());
        assertThat(startCaptor.getValue()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(endCaptor.getValue()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCategoryName()).isEqualTo("Food");
    }

    @Test
    void getSpendingTrend_monthProvided_usesMonthRange() {
        SecurityTestUtils.setAuthenticatedUser(2L, "b@example.com");

        when(analyticsRepository.getDailySpendingTrend(eq(2L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new SpendingTrendResponse(LocalDate.of(2026, 3, 1), java.math.BigDecimal.ONE)));

        List<SpendingTrendResponse> results = analyticsService.getSpendingTrend(3, 2026);

        verify(analyticsRepository).getDailySpendingTrend(eq(2L), startCaptor.capture(), endCaptor.capture());
        assertThat(startCaptor.getValue()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(endCaptor.getValue()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
    }
}
