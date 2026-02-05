package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.RepositoryBranchMetricsInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetBranchMetricsPort {

    RepositoryBranchMetricsInfo getBranchMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
