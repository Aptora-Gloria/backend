package com.optimetric.github.application.port;

import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.api.response.ReviewDto;
import com.optimetric.github.api.response.ReviewCommentDto;
import com.optimetric.github.api.response.SyncStatusDto;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

/**
 * Service port for Pull Request operations.
 * Provides business logic for retrieving and managing Pull Request data.
 */
public interface PullRequestServicePort {

    /**
     * Retrieves all Pull Requests for a specific repository.
     * 
     * @param login User login for authentication
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return List of Pull Request DTOs
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    List<PullRequestDto> getPullRequestsForRepository(String login, String repoFullName) 
            throws UserNotFoundException, RepositoryNotFoundException;

    /**
     * Retrieves a specific Pull Request by repository and PR number.
     * 
     * @param login User login for authentication
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @param prNumber Pull Request number
     * @return Pull Request DTO
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    PullRequestDto getPullRequest(String login, String repoFullName, Integer prNumber) 
            throws UserNotFoundException, RepositoryNotFoundException;

    /**
     * Retrieves all reviews for a specific Pull Request.
     * 
     * @param login User login for authentication
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @param prNumber Pull Request number
     * @return List of Review DTOs
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    List<ReviewDto> getPullRequestReviews(String login, String repoFullName, Integer prNumber) 
            throws UserNotFoundException, RepositoryNotFoundException;

    /**
     * Retrieves all commits for a specific Pull Request.
     * 
     * @param login User login for authentication
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @param prNumber Pull Request number
     * @return List of Review Comment DTOs (representing commits)
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    List<ReviewCommentDto> getPullRequestCommits(String login, String repoFullName, Integer prNumber) 
            throws UserNotFoundException, RepositoryNotFoundException;

    /**
     * Initiates Pull Request synchronization for a repository.
     * 
     * @param login User login for authentication
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    void initiatePullRequestSync(String login, String repoFullName) 
            throws UserNotFoundException, RepositoryNotFoundException;

    /**
     * Gets the synchronization status for a repository.
     * 
     * @param login User login for authentication
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return Sync status DTO with detailed job information
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    SyncStatusDto getSyncStatus(String login, String repoFullName) 
            throws UserNotFoundException, RepositoryNotFoundException;
}