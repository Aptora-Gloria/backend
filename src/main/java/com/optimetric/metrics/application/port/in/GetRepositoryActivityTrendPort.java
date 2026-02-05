package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.RepositoryActivityTrendInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetRepositoryActivityTrendPort {

    RepositoryActivityTrendInfo getActivityTrend(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
