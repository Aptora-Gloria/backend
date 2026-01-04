package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for repository activity trends.
 */
public record RepositoryActivityTrendDto(
        // Weekly trends (last 12 weeks)
        List<WeeklyActivityDto> weeklyTrend,
        
        // Monthly trends (last 6 months)
        List<MonthlyActivityDto> monthlyTrend,
        
        // Recent activity
        Long totalActivityLast30Days,
        Long totalActivityLast7Days,
        ActivityBreakdownDto last30DaysBreakdown,
        
        // Growth metrics
        BigDecimal activityGrowthRate,  // Percentage change
        String mostActiveWeek,
        String mostActiveMonth,
        
        // Daily breakdown
        Map<String, Long> commitsByDay,  // YYYY-MM-DD -> count
        Map<String, Long> prsByDay,
        Map<String, Long> reviewsByDay
) {
    public record WeeklyActivityDto(
            String week,  // YYYY-WW format
            Long commits,
            Long pullRequests,
            Long reviews,
            Long totalActivity
    ) {}
    
    public record MonthlyActivityDto(
            String month,  // YYYY-MM format
            Long commits,
            Long pullRequests,
            Long reviews,
            Long totalActivity
    ) {}
    
    public record ActivityBreakdownDto(
            Long commits,
            Long pullRequests,
            Long reviews
    ) {}
}

