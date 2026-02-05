package com.optimetric.metrics.application.port;

import com.optimetric.metrics.application.dto.*;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface RepositoryMetricsServicePort {

    RepositoryMetricsInfo getRepositoryMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryOverviewInfo getOverview(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryPrMetricsInfo getPullRequestMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryCommitMetricsInfo getCommitMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryReviewMetricsInfo getReviewMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryBranchMetricsInfo getBranchMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryLanguageMetricsInfo getLanguageMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryActivityTrendInfo getActivityTrend(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryCodeQualityInfo getCodeQuality(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryContributorMetricsInfo getContributorMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryHealthScoreInfo getHealthScore(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
