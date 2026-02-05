package com.optimetric.metrics.application.port.out;

import com.optimetric.metrics.application.dto.RepositoryActivityTrendInfo;
import com.optimetric.metrics.application.dto.RepositoryBranchMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryCodeQualityInfo;
import com.optimetric.metrics.application.dto.RepositoryCommitMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryContributorMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryHealthScoreInfo;
import com.optimetric.metrics.application.dto.RepositoryLanguageMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryOverviewInfo;
import com.optimetric.metrics.application.dto.RepositoryPrMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryReviewMetricsInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface RepositoryMetricsQueryPort {

    RepositoryOverviewInfo getOverviewInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryLanguageMetricsInfo getLanguageMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryHealthScoreInfo getHealthScoreInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryReviewMetricsInfo getReviewMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryBranchMetricsInfo getBranchMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryPrMetricsInfo getPrMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryCommitMetricsInfo getCommitMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryActivityTrendInfo getActivityTrendInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryCodeQualityInfo getCodeQualityInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    RepositoryContributorMetricsInfo getContributorMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
