package com.optimetric.github.application.port.out;

import com.optimetric.github.application.dto.RepositoryReadinessInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;

/**
 * Outbound port for repository sync/readiness status.
 */
public interface RepoSyncStatusQueryPort {

    RepositoryReadinessInfo getReadiness(String fullName) throws RepositoryNotFoundException;
}
