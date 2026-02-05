package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.RepositoryLanguageMetricsInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetLanguageMetricsPort {

    RepositoryLanguageMetricsInfo getLanguageMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
