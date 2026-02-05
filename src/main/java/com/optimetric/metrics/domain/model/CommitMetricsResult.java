package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record CommitMetricsResult(
        long totalCommits,
        long mergeCommits,
        long regularCommits,
        BigDecimal mergeCommitPercentage,
        long commitsLast30Days,
        long commitsLast7Days,
        OffsetDateTime lastCommitDate,
        String mostActiveDay,
        Integer mostActiveHour,
        Map<Integer, Long> commitsByHour,
        Map<String, Long> commitsByDayOfWeek,
        long weekdayCommits,
        long weekendCommits,
        BigDecimal weekdayPercentage,
        BigDecimal weekendPercentage,
        long uniqueAuthors,
        List<AuthorStatsResult> topAuthors,
        Map<String, Long> commitsByAuthor,
        BigDecimal averageMessageLength,
        long longestMessageLength,
        long shortestMessageLength
) {
    public record AuthorStatsResult(
            String authorLogin,
            long commitCount,
            long mergeCommitCount
    ) {}
}
