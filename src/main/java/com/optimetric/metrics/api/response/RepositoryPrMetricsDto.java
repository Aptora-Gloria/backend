package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for repository Pull Request metrics.
 */
public record RepositoryPrMetricsDto(
        // Statistics
        Long totalPullRequests,
        Long openPullRequests,
        Long closedPullRequests,
        Long mergedPullRequests,
        Long draftPullRequests,
        BigDecimal mergeRate,
        
        // Size metrics
        BigDecimal averageAdditions,
        BigDecimal averageDeletions,
        BigDecimal averageChangedFiles,
        BigDecimal averageTotalChanges,
        Long largestPrSize,  // additions + deletions
        Long smallestPrSize,
        
        // Size distribution
        Long smallPrCount,  // < 100 lines
        Long mediumPrCount,  // 100-500 lines
        Long largePrCount,  // > 500 lines
        BigDecimal smallPrPercentage,
        BigDecimal mediumPrPercentage,
        BigDecimal largePrPercentage,
        
        // Activity metrics
        Long prsLast30Days,
        Long prsLast7Days,
        Long mergedLast30Days,
        BigDecimal averageMergeTimeHours,
        
        // Author metrics
        Long uniqueAuthors,
        List<AuthorStatsDto> topAuthors,
        Map<String, Long> prsByAuthor  // author -> count
) {
    public record AuthorStatsDto(
            String authorLogin,
            Long prCount,
            Long mergedPrCount,
            BigDecimal mergeRate
    ) {}
}

