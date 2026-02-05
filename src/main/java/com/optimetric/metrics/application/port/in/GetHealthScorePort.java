package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.RepositoryHealthScoreInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetHealthScorePort {

    RepositoryHealthScoreInfo getHealthScore(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
