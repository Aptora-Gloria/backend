package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.RepositoryContributorMetricsInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetContributorMetricsPort {

    RepositoryContributorMetricsInfo getContributorMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
