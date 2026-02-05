package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PrMetricsResult(
        long totalPullRequests,
        long openPullRequests,
        long closedPullRequests,
        long mergedPullRequests,
        long draftPullRequests,
        BigDecimal mergeRate,
        BigDecimal averageAdditions,
        BigDecimal averageDeletions,
        long totalAdditions,
        long totalDeletions,
        BigDecimal averageChangedFiles,
        BigDecimal averageTotalChanges,
        long largestPrSize,
        long smallestPrSize,
        long smallPrCount,
        long mediumPrCount,
        long largePrCount,
        BigDecimal smallPrPercentage,
        BigDecimal mediumPrPercentage,
        BigDecimal largePrPercentage,
        long prsLast30Days,
        long prsLast7Days,
        long mergedLast30Days,
        BigDecimal averageMergeTimeHours,
        long uniqueAuthors,
        List<AuthorStatsResult> topAuthors,
        Map<String, Long> prsByAuthor
) {
    public record AuthorStatsResult(
            String authorLogin,
            long prCount,
            long mergedPrCount,
            BigDecimal mergeRate
    ) {}
}
