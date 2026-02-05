package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.RepositoryPrMetricsInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetPullRequestMetricsPort {

    RepositoryPrMetricsInfo getPullRequestMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
