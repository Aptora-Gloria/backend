package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record RepositoryCommitMetricsInfo(
        Long totalCommits,
        Long mergeCommits,
        Long regularCommits,
        BigDecimal mergeCommitPercentage,
        Long commitsLast30Days,
        Long commitsLast7Days,
        OffsetDateTime lastCommitDate,
        String mostActiveDay,
        Integer mostActiveHour,
        Map<Integer, Long> commitsByHour,
        Map<String, Long> commitsByDayOfWeek,
        Long weekdayCommits,
        Long weekendCommits,
        BigDecimal weekdayPercentage,
        BigDecimal weekendPercentage,
        Long uniqueAuthors,
        List<AuthorStatsInfo> topAuthors,
        Map<String, Long> commitsByAuthor,
        BigDecimal averageMessageLength,
        Long longestMessageLength,
        Long shortestMessageLength
) {
    public record AuthorStatsInfo(
            String authorLogin,
            Long commitCount,
            Long mergeCommitCount
    ) {}
}
