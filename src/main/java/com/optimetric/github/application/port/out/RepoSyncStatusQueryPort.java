package com.optimetric.github.application.port.out;

import com.optimetric.github.application.dto.RepositoryReadinessInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;

public interface RepoSyncStatusQueryPort {

    RepositoryReadinessInfo getReadiness(String fullName) throws RepositoryNotFoundException;
}
