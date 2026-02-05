package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Katkıda bulunan metrikleri hesaplama sonucu; saf Java.
public record ContributorMetricsResult(
        long totalContributors,
        long activeContributorsLast30Days,
        long activeContributorsLast7Days,
        List<ContributorStatsResult> topContributorsByCommits,
        List<ContributorStatsResult> topContributorsByPrs,
        List<ContributorStatsResult> topContributorsByReviews,
        Map<String, Long> commitsByContributor,
        Map<String, Long> prsByContributor,
        Map<String, Long> reviewsByContributor,
        BigDecimal top10ContributorsPercentage,
        long singleCommitContributors,
        long regularContributors
) {
    public record ContributorStatsResult(String contributorLogin, long commitCount, long prCount, long reviewCount, long totalContributions) {}
}
