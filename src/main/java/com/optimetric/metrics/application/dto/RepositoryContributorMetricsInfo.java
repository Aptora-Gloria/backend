package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RepositoryContributorMetricsInfo(
        Long totalContributors,
        Long activeContributorsLast30Days,
        Long activeContributorsLast7Days,
        List<ContributorStatsInfo> topContributorsByCommits,
        List<ContributorStatsInfo> topContributorsByPrs,
        List<ContributorStatsInfo> topContributorsByReviews,
        Map<String, Long> commitsByContributor,
        Map<String, Long> prsByContributor,
        Map<String, Long> reviewsByContributor,
        BigDecimal top10ContributorsPercentage,
        Long singleCommitContributors,
        Long regularContributors
) {
    public record ContributorStatsInfo(
            String contributorLogin,
            Long commitCount,
            Long prCount,
            Long reviewCount,
            Long totalContributions
    ) {}
}
