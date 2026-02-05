package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.RepositoryReadinessInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;

public interface GetRepoSyncStatusPort {

    RepositoryReadinessInfo getReadiness(String fullName) throws RepositoryNotFoundException;
}
