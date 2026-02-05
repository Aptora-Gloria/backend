package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Aktivite trendi hesaplama sonucu; saf Java.
public record ActivityTrendResult(
        List<WeeklyActivityResult> weeklyTrend,
        List<MonthlyActivityResult> monthlyTrend,
        long totalActivityLast30Days,
        long totalActivityLast7Days,
        ActivityBreakdownResult last30DaysBreakdown,
        BigDecimal activityGrowthRate,
        String mostActiveWeek,
        String mostActiveMonth,
        Map<String, Long> commitsByDay,
        Map<String, Long> prsByDay,
        Map<String, Long> reviewsByDay
) {
    public record WeeklyActivityResult(String week, long commits, long pullRequests, long reviews, long totalActivity) {}
    public record MonthlyActivityResult(String month, long commits, long pullRequests, long reviews, long totalActivity) {}
    public record ActivityBreakdownResult(long commits, long pullRequests, long reviews) {}
}
