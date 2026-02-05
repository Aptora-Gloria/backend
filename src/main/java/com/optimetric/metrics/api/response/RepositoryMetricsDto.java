package com.optimetric.metrics.api.response;

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

