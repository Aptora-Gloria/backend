package com.optimetric.github.api;

import com.optimetric.github.api.request.GetPullRequestRequest;
import com.optimetric.github.api.request.GetPullRequestsRequest;
import com.optimetric.github.api.request.GetSyncStatusRequest;
import com.optimetric.github.api.request.InitiatePullRequestSyncRequest;
import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.api.response.ReviewCommentDto;
import com.optimetric.github.api.response.ReviewDto;
import com.optimetric.github.api.response.SyncStatusDto;
import com.optimetric.github.application.port.PullRequestServicePort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Pull Request operations.
 * Provides endpoints for retrieving PR data, managing synchronization, and accessing PR details.
 * All endpoints require authentication and validate repository access.
 */
@RestController
@RequestMapping("/api/github/pull-requests")
@RequiredArgsConstructor
public class PullRequestController {

    private final PullRequestServicePort pullRequestService;

    /**
     * Retrieves all Pull Requests for a specific repository.
     * 
     * @param login Authenticated user login
     * @param request Request containing repository full name
     * @return List of Pull Request DTOs with data level and sync status
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    @PostMapping("/query")
    public ResponseEntity<List<PullRequestDto>> getPullRequests(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetPullRequestsRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        List<PullRequestDto> pullRequests = pullRequestService.getPullRequestsForRepository(
                login, 
                request.repoFullName()
        );
        return ResponseEntity.ok(pullRequests);
    }

    /**
     * Retrieves a specific Pull Request by repository and PR number.
     * 
     * @param login Authenticated user login
     * @param request Request containing repository full name and PR number
     * @return Pull Request DTO with complete information
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    @PostMapping("/query/single")
    public ResponseEntity<PullRequestDto> getPullRequest(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetPullRequestRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        PullRequestDto pullRequest = pullRequestService.getPullRequest(
                login, 
                request.repoFullName(), 
                request.prNumber()
        );
        return ResponseEntity.ok(pullRequest);
    }

    /**
     * Retrieves all reviews for a specific Pull Request.
     * 
     * @param login Authenticated user login
     * @param request Request containing repository full name and PR number
     * @return List of Review DTOs
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    @PostMapping("/reviews")
    public ResponseEntity<List<ReviewDto>> getPullRequestReviews(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetPullRequestRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        List<ReviewDto> reviews = pullRequestService.getPullRequestReviews(
                login, 
                request.repoFullName(), 
                request.prNumber()
        );
        return ResponseEntity.ok(reviews);
    }

    /**
     * Retrieves all commits for a specific Pull Request.
     * 
     * @param login Authenticated user login
     * @param request Request containing repository full name and PR number
     * @return List of Commit DTOs (represented as ReviewCommentDto)
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    @PostMapping("/commits")
    public ResponseEntity<List<ReviewCommentDto>> getPullRequestCommits(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetPullRequestRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        List<ReviewCommentDto> commits = pullRequestService.getPullRequestCommits(
                login, 
                request.repoFullName(), 
                request.prNumber()
        );
        return ResponseEntity.ok(commits);
    }

    /**
     * Initiates Pull Request synchronization for a repository.
     * Starts the ingestion pipeline to collect PR data from GitHub API.
     * 
     * @param login Authenticated user login
     * @param request Request containing repository full name
     * @return Success response
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    @PostMapping("/sync/initiate")
    public ResponseEntity<Void> initiatePullRequestSync(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid InitiatePullRequestSyncRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        pullRequestService.initiatePullRequestSync(login, request.repoFullName());
        return ResponseEntity.ok().build();
    }

    /**
     * Gets the synchronization status for a repository.
     * Provides detailed information about ongoing and completed sync jobs.
     * 
     * @param login Authenticated user login
     * @param request Request containing repository full name
     * @return Sync status DTO with job details and timestamps
     * @throws UserNotFoundException if user not found
     * @throws RepositoryNotFoundException if repository not found or not accessible
     */
    @PostMapping("/sync/status")
    public ResponseEntity<SyncStatusDto> getSyncStatus(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetSyncStatusRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        SyncStatusDto syncStatus = pullRequestService.getSyncStatus(login, request.repoFullName());
        return ResponseEntity.ok(syncStatus);
    }
}