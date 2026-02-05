package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.RepositoryCommitMetricsInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetCommitMetricsPort {

    RepositoryCommitMetricsInfo getCommitMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
