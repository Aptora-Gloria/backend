package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ActivityTrendDto(
        List<WeeklyActivityDto> weeklyTrend,  // Last 12 weeks
        List<MonthlyActivityDto> monthlyTrend,  // Last 6 months
        Long totalActivityLast30Days,
        Long totalActivityLast7Days,
        BigDecimal activityGrowthRate,  // Percentage change
        String mostActiveWeek,
        String mostActiveMonth,
        ActivityBreakdownDto last30DaysBreakdown
) {
    public record WeeklyActivityDto(
            String week,  // YYYY-Www formatı
            Long commits,
            Long pullRequests,
            Long reviews,
            Long totalActivity
    ) {}
    
    public record MonthlyActivityDto(
            String month,  // YYYY-MM formatı
            Long commits,
            Long pullRequests,
            Long reviews,
            Long totalActivity
    ) {}
    
    public record ActivityBreakdownDto(
            Long commits,
            Long pullRequests,
            Long reviews,
            Map<String, Long> commitsByDay,
            Map<String, Long> prsByDay
    ) {}
}

