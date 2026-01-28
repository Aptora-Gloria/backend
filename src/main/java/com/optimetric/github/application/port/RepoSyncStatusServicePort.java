package com.optimetric.github.application.port;

import com.optimetric.github.api.response.RepositoryReadinessDto;
import com.optimetric.shared.exception.RepositoryNotFoundException;

/**
 * Port for repository sync status service.
 * Provides methods to check repository data readiness.
 */
public interface RepoSyncStatusServicePort {
    
    /**
     * Gets the readiness status for a repository identified by full name.
     * 
     * @param fullName Repository full name (e.g., "owner/repo")
     * @return RepositoryReadinessDto containing state, missing components, and flags
     * @throws RepositoryNotFoundException if repository not found
     */
    RepositoryReadinessDto getReadiness(String fullName) throws RepositoryNotFoundException;
}
