package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.RepositoryCodeQualityInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetCodeQualityPort {

    RepositoryCodeQualityInfo getCodeQuality(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
}
