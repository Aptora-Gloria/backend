package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.RepositoryOverviewInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetOverviewPort {

    RepositoryOverviewInfo getOverview(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
