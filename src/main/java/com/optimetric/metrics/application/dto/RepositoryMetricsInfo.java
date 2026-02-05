package com.optimetric.metrics.application.dto;

public record RepositoryMetricsInfo(
        RepositoryOverviewInfo overview,
        RepositoryPrMetricsInfo pullRequests,
        RepositoryCommitMetricsInfo commits,
        RepositoryReviewMetricsInfo reviews,
        RepositoryBranchMetricsInfo branches,
        RepositoryLanguageMetricsInfo languages,
        RepositoryActivityTrendInfo activityTrend,
        RepositoryCodeQualityInfo codeQuality,
        RepositoryContributorMetricsInfo contributors,
        RepositoryHealthScoreInfo healthScore
) {}
