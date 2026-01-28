package com.optimetric.github.application.port;

import com.optimetric.github.api.response.RepositoryAnalyzeResponse;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

/**
 * Port for repository analyze service.
 * Analyzes repository data readiness and enqueues sync jobs for missing components.
 */
public interface RepositoryAnalyzeServicePort {
    
    /**
     * Analyzes repository data readiness and enqueues sync jobs for missing components.
     * 
     * @param login User login (for authentication and job execution)
     * @param fullName Repository full name (e.g., "owner/repo")
     * @return RepositoryAnalyzeResponse containing state, enqueued jobs, and status endpoint
     * @throws RepositoryNotFoundException if repository not found
     * @throws UserNotFoundException if user not found
     */
    RepositoryAnalyzeResponse analyze(String login, String fullName) throws RepositoryNotFoundException, UserNotFoundException;
}
