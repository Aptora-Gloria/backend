package com.optimetric.metrics.application.port.out;

import com.optimetric.metrics.domain.model.ActivityTrendInput;
import com.optimetric.metrics.domain.model.BranchMetricsInput;
import com.optimetric.metrics.domain.model.CommitMetricsInput;
import com.optimetric.metrics.domain.model.ContributorMetricsInput;
import com.optimetric.metrics.domain.model.HealthScoreInput;
import com.optimetric.metrics.domain.model.LanguageMetricsInput;
import com.optimetric.metrics.domain.model.OverviewInput;
import com.optimetric.metrics.domain.model.PrMetricsInput;
import com.optimetric.metrics.domain.model.ReviewMetricsInput;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface RepositoryMetricsDataPort {

    PrMetricsInput getPrMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    CommitMetricsInput getCommitMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    ReviewMetricsInput getReviewMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    BranchMetricsInput getBranchMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    LanguageMetricsInput getLanguageMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    ActivityTrendInput getActivityTrendInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    ContributorMetricsInput getContributorMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    OverviewInput getOverviewInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    HealthScoreInput getHealthScoreInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
