package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;
import java.util.Map;

public record CommitTimeAnalysisInfo(
        Map<Integer, Long> commitsByHour,
        Map<String, Long> commitsByDayOfWeek,
        Long weekdayCommits,
        Long weekendCommits,
        BigDecimal weekdayPercentage,
        BigDecimal weekendPercentage,
        Integer mostActiveHour,
        String mostActiveDay,
        Long last30DaysCommits,
        Long last7DaysCommits
) {}
