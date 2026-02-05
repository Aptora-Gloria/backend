package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
        Long singleCommitContributors,  // Yalnızca 1 commit'i olan katkıda bulunanlar
        Long regularContributors  // 5+ commit'i olan düzenli katkıda bulunanlar
) {
    public record ContributorStatsDto(
            String contributorLogin,
            Long commitCount,
            Long prCount,
            Long reviewCount,
            Long totalContributions
    ) {}
}

