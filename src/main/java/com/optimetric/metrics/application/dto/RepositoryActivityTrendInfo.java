package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RepositoryActivityTrendInfo(
        List<WeeklyActivityInfo> weeklyTrend,
        List<MonthlyActivityInfo> monthlyTrend,
        Long totalActivityLast30Days,
        Long totalActivityLast7Days,
        ActivityBreakdownInfo last30DaysBreakdown,
        BigDecimal activityGrowthRate,
        String mostActiveWeek,
        String mostActiveMonth,
        Map<String, Long> commitsByDay,
        Map<String, Long> prsByDay,
        Map<String, Long> reviewsByDay
) {
    public record WeeklyActivityInfo(
            String week,
            Long commits,
            Long pullRequests,
            Long reviews,
            Long totalActivity
    ) {}

    public record MonthlyActivityInfo(
            String month,
            Long commits,
            Long pullRequests,
            Long reviews,
            Long totalActivity
    ) {}

    public record ActivityBreakdownInfo(
            Long commits,
            Long pullRequests,
            Long reviews
    ) {}
}
