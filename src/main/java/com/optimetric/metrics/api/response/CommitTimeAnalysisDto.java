package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.util.Map;

public record CommitTimeAnalysisDto(
        Map<Integer, Long> commitsByHour,  // hour (0-23) -> count
        Map<String, Long> commitsByDayOfWeek,  // MONDAY, TUESDAY, etc. -> count
        Long weekdayCommits,
        Long weekendCommits,
        BigDecimal weekdayPercentage,
        BigDecimal weekendPercentage,
        Integer mostActiveHour,
        String mostActiveDay,
        Long last30DaysCommits,
        Long last7DaysCommits
) {}

