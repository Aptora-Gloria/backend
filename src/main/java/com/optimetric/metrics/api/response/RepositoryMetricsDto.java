package com.optimetric.metrics.api.response;

/**
 * Comprehensive DTO containing all repository metrics.
 */
public record RepositoryMetricsDto(
        RepositoryOverviewDto overview,
        RepositoryPrMetricsDto pullRequests,
        RepositoryCommitMetricsDto commits,
        RepositoryReviewMetricsDto reviews,
        RepositoryBranchMetricsDto branches,
        RepositoryLanguageMetricsDto languages,
        RepositoryActivityTrendDto activityTrend,
        RepositoryCodeQualityDto codeQuality,
        RepositoryContributorMetricsDto contributors,
        RepositoryHealthScoreDto healthScore
) {}

