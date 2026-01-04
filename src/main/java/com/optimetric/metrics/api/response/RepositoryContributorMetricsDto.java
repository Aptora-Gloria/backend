package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for repository contributor metrics.
 */
public record RepositoryContributorMetricsDto(
        Long totalContributors,
        Long activeContributorsLast30Days,
        Long activeContributorsLast7Days,
        
        // Top contributors
        List<ContributorStatsDto> topContributorsByCommits,
        List<ContributorStatsDto> topContributorsByPrs,
        List<ContributorStatsDto> topContributorsByReviews,
        
        // Contributor distribution
        Map<String, Long> commitsByContributor,  // contributor -> count
        Map<String, Long> prsByContributor,
        Map<String, Long> reviewsByContributor,
        
        // Activity distribution (Pareto analysis)
        BigDecimal top10ContributorsPercentage,  // Top 10 contributors' activity %
        Long singleCommitContributors,  // Contributors with only 1 commit
        Long regularContributors  // Contributors with 5+ commits
) {
    public record ContributorStatsDto(
            String contributorLogin,
            Long commitCount,
            Long prCount,
            Long reviewCount,
            Long totalContributions
    ) {}
}

