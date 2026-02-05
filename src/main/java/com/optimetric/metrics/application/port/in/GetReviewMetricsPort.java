package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.RepositoryReviewMetricsInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetReviewMetricsPort {

    RepositoryReviewMetricsInfo getReviewMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
