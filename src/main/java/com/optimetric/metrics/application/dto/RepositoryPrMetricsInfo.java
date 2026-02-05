package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RepositoryPrMetricsInfo(
        Long totalPullRequests,
        Long openPullRequests,
        Long closedPullRequests,
        Long mergedPullRequests,
        Long draftPullRequests,
        BigDecimal mergeRate,
        BigDecimal averageAdditions,
        BigDecimal averageDeletions,
        BigDecimal averageChangedFiles,
        BigDecimal averageTotalChanges,
        Long largestPrSize,
        Long smallestPrSize,
        Long smallPrCount,
        Long mediumPrCount,
        Long largePrCount,
        BigDecimal smallPrPercentage,
        BigDecimal mediumPrPercentage,
        BigDecimal largePrPercentage,
        Long prsLast30Days,
        Long prsLast7Days,
        Long mergedLast30Days,
        BigDecimal averageMergeTimeHours,
        Long uniqueAuthors,
        List<AuthorStatsInfo> topAuthors,
        Map<String, Long> prsByAuthor
) {
    public record AuthorStatsInfo(
            String authorLogin,
            Long prCount,
            Long mergedPrCount,
            BigDecimal mergeRate
    ) {}
}
