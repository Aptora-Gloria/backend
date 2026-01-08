package com.optimetric.github.application.client;

import java.util.List;

import com.optimetric.github.application.dto.RepositoryInitData;

/**
 * Outbound port for querying GitHub repositories.
 * Provides abstraction for GitHub API access, returning application-level DTOs.
 */
public interface GitHubRepoQueryPort {
    /**
     * Fetches user repositories from GitHub API.
     *
     * @param accessToken GitHub access token
     * @return List of repository initialization data (application DTO)
     */
    List<RepositoryInitData> getUserRepos(String accessToken);
}
