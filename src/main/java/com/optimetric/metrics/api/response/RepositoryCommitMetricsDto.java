package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record RepositoryCommitMetricsDto(
        // Statistics
        Long totalCommits,
        Long mergeCommits,
        Long regularCommits,
        BigDecimal mergeCommitPercentage,
        
        // Activity metrics
        Long commitsLast30Days,
        Long commitsLast7Days,
        OffsetDateTime lastCommitDate,
        String mostActiveDay,  // Day of week
        Integer mostActiveHour,  // 0-23
        
        // Time analysis
        Map<Integer, Long> commitsByHour,  // hour -> count
        Map<String, Long> commitsByDayOfWeek,  // day -> count
        Long weekdayCommits,
        Long weekendCommits,
        BigDecimal weekdayPercentage,
        BigDecimal weekendPercentage,
        
        // Author metrics
        Long uniqueAuthors,
        List<AuthorStatsDto> topAuthors,
        Map<String, Long> commitsByAuthor,  // author -> count
        
        // Message analysis
        BigDecimal averageMessageLength,
        Long longestMessageLength,
        Long shortestMessageLength
) {
    public record AuthorStatsDto(
            String authorLogin,
            Long commitCount,
            Long mergeCommitCount
    ) {}
}

