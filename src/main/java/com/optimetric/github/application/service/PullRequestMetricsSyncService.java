package com.optimetric.github.application.service;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.api.response.CommitDto;
import com.optimetric.github.api.response.PullRequestCommentDto;
import com.optimetric.github.api.response.PullRequestReviewDto;
import com.optimetric.github.api.response.SyncMetricsResponse;
import com.optimetric.github.application.client.PullRequestClientPort;
import com.optimetric.github.application.port.RepoServicePort;
import com.optimetric.github.infrastructure.core.mapper.CommitMapper;
import com.optimetric.github.infrastructure.core.mapper.PullRequestCommentMapper;
import com.optimetric.github.infrastructure.core.mapper.PullRequestReviewMapper;
import com.optimetric.github.infrastructure.persistence.adapter.PullRequestCommentPersistenceService;
import com.optimetric.github.infrastructure.persistence.adapter.PullRequestCommitPersistenceService;
import com.optimetric.github.infrastructure.persistence.adapter.PullRequestPersistenceService;
import com.optimetric.github.infrastructure.persistence.adapter.PullRequestReviewPersistenceService;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestCommentRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestReviewRepository;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator service for syncing Pull Request metrics.
 * This service coordinates the flow of fetching and persisting PR details, commits, reviews, and comments.
 * Single responsibility: Orchestrate the metrics sync flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestMetricsSyncService {

    private final PullRequestClientPort pullRequestClient;
    private final AuthServicePort authServicePort;
    private final RepoServicePort repoServicePort;
    private final PullRequestRepository pullRequestRepository;
    private final PullRequestReviewRepository reviewRepository;
    private final PullRequestCommentRepository commentRepository;
    private final PullRequestPersistenceService pullRequestPersistenceService;
    private final PullRequestCommitPersistenceService commitPersistenceService;
    private final PullRequestReviewPersistenceService reviewPersistenceService;
    private final PullRequestCommentPersistenceService commentPersistenceService;

    /**
     * Syncs metrics for all pull requests in a repository and returns detailed response.
     * 
     * @param login User login
     * @param repositoryFullName Repository full name (owner/repo)
     * @return SyncMetricsResponse containing sync results with commits, reviews, and comments
     * @throws RepositoryNotFoundException if repository not found
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public SyncMetricsResponse syncMetricsForRepository(String login, String repositoryFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        log.info("Starting metrics sync for repository: {}", repositoryFullName);

        // Get repository
        RepositoryEntity repository = repoServicePort.getByFullNameForCurrentUserOrThrow(login, repositoryFullName);

        // Get all PRs for this repository
        List<PullRequestEntity> pullRequests = pullRequestRepository.findByRepository(repository);
        if (pullRequests.isEmpty()) {
            log.info("No pull requests found for repository: {}", repositoryFullName);
            return new SyncMetricsResponse(
                    repositoryFullName,
                    0,
                    List.of()
            );
        }

        // Get access token
        String accessToken = authServicePort.getAccessTokenFromLogin(login);
        String owner = repository.getOwnerLogin();
        String repoName = repository.getName();

        // Sync metrics for each PR
        for (PullRequestEntity pullRequest : pullRequests) {
            try {
                syncMetricsForPullRequest(accessToken, owner, repoName, pullRequest, repository);
            } catch (Exception e) {
                log.error("Error syncing metrics for PR #{}: {}", pullRequest.getNumber(), e.getMessage(), e);
                // Continue with next PR instead of failing entire sync
            }
        }

        // Refresh PRs to get updated relationships
        pullRequestRepository.flush();
        pullRequests = pullRequestRepository.findByRepository(repository);

        // Build response with commits, reviews, and comments for each PR
        // Note: We're still in a transaction, so lazy loading will work
        List<SyncMetricsResponse.PullRequestMetrics> prMetrics = new ArrayList<>();
        for (PullRequestEntity pr : pullRequests) {
            // Trigger lazy loading of commits by accessing the collection
            if (pr.getCommits() != null) {
                pr.getCommits().size(); // Trigger lazy loading
            }
            prMetrics.add(buildPullRequestMetrics(pr));
        }

        log.info("Completed metrics sync for repository: {}", repositoryFullName);
        return new SyncMetricsResponse(
                repositoryFullName,
                pullRequests.size(),
                prMetrics
        );
    }

    /**
     * Builds PullRequestMetrics DTO from PullRequestEntity, including commits, reviews, and comments.
     */
    private SyncMetricsResponse.PullRequestMetrics buildPullRequestMetrics(PullRequestEntity pr) {
        // Get commits (from ManyToMany relationship)
        List<CommitDto> commits = pr.getCommits() != null
                ? CommitMapper.toDtos(new ArrayList<>(pr.getCommits()))
                : List.of();

        // Get reviews
        List<PullRequestReviewDto> reviews = PullRequestReviewMapper.toDtos(
                reviewRepository.findByPullRequest(pr)
        );

        // Get comments
        List<PullRequestCommentDto> comments = PullRequestCommentMapper.toDtos(
                commentRepository.findByPullRequest(pr)
        );

        return new SyncMetricsResponse.PullRequestMetrics(
                pr.getNumber(),
                pr.getTitle(),
                pr.getState(),
                pr.getCommitCount() != null ? pr.getCommitCount() : 0,
                pr.getReviewCount() != null ? pr.getReviewCount() : 0,
                pr.getCommentCount() != null ? pr.getCommentCount() : 0,
                commits,
                reviews,
                comments
        );
    }

    /**
     * Syncs metrics for a single pull request.
     * 
     * @param accessToken GitHub access token
     * @param owner Repository owner
     * @param repo Repository name
     * @param pullRequest Pull request entity
     * @param repository Repository entity
     */
    private void syncMetricsForPullRequest(
            String accessToken,
            String owner,
            String repo,
            PullRequestEntity pullRequest,
            RepositoryEntity repository
    ) {
        Integer pullNumber = pullRequest.getNumber();
        log.debug("Syncing metrics for PR #{}", pullNumber);

        // 1. Get PR details (additions, deletions, changed_files, merged, merged_at)
        try {
            var details = pullRequestClient.getPullRequestDetails(accessToken, owner, repo, pullNumber);
            pullRequestPersistenceService.updateDetails(pullRequest, details);
            log.debug("Updated details for PR #{}", pullNumber);
        } catch (Exception e) {
            log.warn("Failed to fetch details for PR #{}: {}", pullNumber, e.getMessage());
        }

        // 2. Get PR commits and sync
        try {
            var commits = pullRequestClient.getPullRequestCommits(accessToken, owner, repo, pullNumber);
            commitPersistenceService.syncCommits(pullRequest, commits, repository);
            log.debug("Synced {} commits for PR #{}", commits != null ? commits.size() : 0, pullNumber);
        } catch (Exception e) {
            log.warn("Failed to fetch commits for PR #{}: {}", pullNumber, e.getMessage());
        }

        // 3. Get PR reviews and sync
        try {
            var reviews = pullRequestClient.getPullRequestReviews(accessToken, owner, repo, pullNumber);
            reviewPersistenceService.syncReviews(pullRequest, reviews);
            log.debug("Synced {} reviews for PR #{}", reviews != null ? reviews.size() : 0, pullNumber);
        } catch (Exception e) {
            log.warn("Failed to fetch reviews for PR #{}: {}", pullNumber, e.getMessage());
        }

        // 4. Get PR comments and sync
        try {
            var comments = pullRequestClient.getPullRequestComments(accessToken, owner, repo, pullNumber);
            commentPersistenceService.syncComments(pullRequest, comments);
            log.debug("Synced {} comments for PR #{}", comments != null ? comments.size() : 0, pullNumber);
        } catch (Exception e) {
            log.warn("Failed to fetch comments for PR #{}: {}", pullNumber, e.getMessage());
        }
    }
}

