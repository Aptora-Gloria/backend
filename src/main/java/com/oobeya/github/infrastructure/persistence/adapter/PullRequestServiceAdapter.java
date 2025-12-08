package com.oobeya.github.infrastructure.persistence.adapter;

import com.oobeya.auth.application.AuthServicePort;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.PullRequestServicePort;
import com.oobeya.github.api.response.GitHubPullRequestResponse;
import com.oobeya.github.dto.request.PullRequestListRequest;
import com.oobeya.github.dto.request.PullRequestSyncRequest;
import com.oobeya.github.dto.response.PullRequestDto;
import com.oobeya.github.infrastructure.core.mapper.PullRequestMapper;
import com.oobeya.github.infrastructure.persistence.entity.BranchEntity;
import com.oobeya.github.infrastructure.persistence.entity.PullRequestEntity;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;
import com.oobeya.github.infrastructure.persistence.jpa.BranchRepository;
import com.oobeya.github.infrastructure.persistence.jpa.PullRequestRepository;
import com.oobeya.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.oobeya.shared.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service adapter for Pull Request operations.
 * Handles fetching from GitHub API, mapping, and persistence.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestServiceAdapter implements PullRequestServicePort {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final ApiClientPort apiClient;
    private final AuthServicePort authServicePort;
    private final PullRequestRepository pullRequestRepository;
    private final RepositoryRepository repositoryRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestDto> listPullRequests(PullRequestListRequest request) {
        try {
            Pageable pageable = PageRequest.of(
                    request.page() != null ? request.page() : 0,
                    request.size() != null ? request.size() : 20
            );

            Page<PullRequestEntity> page;
            if (request.status() != null) {
                page = pullRequestRepository.findByRepositoryIdAndStatus(
                        request.repositoryId(),
                        request.status(),
                        pageable
                );
            } else {
                page = pullRequestRepository.findByRepositoryId(request.repositoryId(), pageable);
            }

            List<PullRequestEntity> entities = page.getContent();

            // Filter by merged status if specified
            if (request.merged() != null) {
                entities = entities.stream()
                        .filter(pr -> {
                            boolean isMerged = pr.getStatus() == PullRequestEntity.PullRequestStatus.MERGED;
                            return request.merged().equals(isMerged);
                        })
                        .toList();
            }

            return PullRequestMapper.entitiesToDtos(entities);
        } catch (Exception e) {
            log.error("Error listing pull requests for repository {}: {}", request.repositoryId(), e.getMessage(), e);
            throw new GitHubApiException("Failed to list pull requests: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PullRequestDto> getPullRequestDetail(Long repositoryId, Integer pullRequestNumber) {
        try {
            Optional<PullRequestEntity> entity = pullRequestRepository.findByRepositoryIdAndNumber(
                    repositoryId,
                    pullRequestNumber
            );

            return entity.map(PullRequestMapper::toDto);
        } catch (Exception e) {
            log.error("Error fetching pull request detail for repository {} PR #{}: {}",
                    repositoryId, pullRequestNumber, e.getMessage(), e);
            throw new GitHubApiException("Failed to get pull request detail: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void syncPullRequests(PullRequestSyncRequest request) {
        try {
            RepositoryEntity repository = repositoryRepository.findById(request.repositoryId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Repository not found with id: " + request.repositoryId()));

            String accessToken = authServicePort.getAccessTokenFromToken(request.authHeader());
            if (accessToken == null || accessToken.isEmpty()) {
                throw new GitHubApiException("Access token is required for syncing pull requests");
            }

            String state = "all"; // Fetch all states
            int perPage = 100;
            boolean hasMore = true;
            int currentPage = 1;

            while (hasMore) {
                final int page = currentPage; // Make effectively final for lambda
                List<GitHubPullRequestResponse> pullRequestResponses = executeWithRetry(() ->
                        apiClient.getPullRequestsRaw(
                                accessToken,
                                request.owner(),
                                request.repositoryName(),
                                state,
                                page,
                                perPage
                        )
                );

                if (pullRequestResponses == null || pullRequestResponses.isEmpty()) {
                    hasMore = false;
                    break;
                }

                savePullRequestsFromResponse(pullRequestResponses, repository, request.syncSince(), accessToken);

                if (pullRequestResponses.size() < perPage) {
                    hasMore = false;
                } else {
                    currentPage++;
                }
            }

            log.info("Successfully synced pull requests for repository {} ({}/{})",
                    request.repositoryId(), request.owner(), request.repositoryName());
        } catch (GitHubApiException e) {
            log.error("GitHub API error while syncing pull requests: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error syncing pull requests for repository {}: {}",
                    request.repositoryId(), e.getMessage(), e);
            throw new GitHubApiException("Failed to sync pull requests: " + e.getMessage(), e);
        }
    }

    private void savePullRequestsFromResponse(
            List<GitHubPullRequestResponse> responses,
            RepositoryEntity repository,
            OffsetDateTime syncSince,
            String accessToken
    ) {
        for (GitHubPullRequestResponse response : responses) {
            try {
                // Skip if syncSince is specified and PR is older
                OffsetDateTime createdAt = PullRequestMapper.parseTime(response.createdAt());
                if (syncSince != null && createdAt != null && createdAt.isBefore(syncSince)) {
                    continue;
                }

                // Check if PR already exists
                Optional<PullRequestEntity> existing = pullRequestRepository.findByGithubPrId(response.id());

                if (existing.isPresent()) {
                    // Update existing PR
                    PullRequestEntity entity = existing.get();
                    updateEntityFromResponse(entity, response);
                    pullRequestRepository.save(entity);
                } else {
                    // Create new PR with proper branch mapping
                    PullRequestEntity newEntity = createEntityFromResponse(response, repository, accessToken);
                    pullRequestRepository.save(newEntity);
                }
            } catch (Exception e) {
                log.warn("Error saving pull request {}: {}", response.number(), e.getMessage(), e);
                // Continue with other PRs
            }
        }
    }

    /**
     * Creates or finds branches for PR source and target.
     * Uses branch names from GitHub API response (head.ref and base.ref).
     */
    private PullRequestEntity createEntityFromResponse(
            GitHubPullRequestResponse response,
            RepositoryEntity repository,
            String accessToken
    ) {
        // Get or create source branch (head)
        BranchEntity sourceBranch = findOrCreateBranch(
                response.head() != null ? response.head().ref() : null,
                repository,
                accessToken
        );

        // Get or create target branch (base)
        BranchEntity targetBranch = findOrCreateBranch(
                response.base() != null ? response.base().ref() : null,
                repository,
                accessToken
        );

        if (sourceBranch == null || targetBranch == null) {
            throw new IllegalStateException(
                    String.format("Failed to resolve branches for PR #%d. Source: %s, Target: %s",
                            response.number(),
                            response.head() != null ? response.head().ref() : "null",
                            response.base() != null ? response.base().ref() : "null")
            );
        }

        return PullRequestMapper.toEntity(response, repository, sourceBranch, targetBranch);
    }

    /**
     * Finds existing branch by name or creates it if it doesn't exist.
     */
    private BranchEntity findOrCreateBranch(String branchName, RepositoryEntity repository, String accessToken) {
        // Use default branch if branchName is null or blank
        final String finalBranchName = (branchName == null || branchName.isBlank())
                ? repository.getDefaultBranch()
                : branchName;

        // Try to find existing branch
        List<BranchEntity> existingBranches = branchRepository.findAllByRepository_Id(repository.getId());
        Optional<BranchEntity> existing = existingBranches.stream()
                .filter(b -> b.getName().equals(finalBranchName))
                .findFirst();

        if (existing.isPresent()) {
            return existing.get();
        }

        // Branch doesn't exist, create it
        BranchEntity newBranch = BranchEntity.builder()
                .name(finalBranchName)
                .repository(repository)
                .build();

        return branchRepository.save(newBranch);
    }

    private void updateEntityFromResponse(PullRequestEntity entity, GitHubPullRequestResponse response) {
        entity.setTitle(response.title());
        entity.setBody(response.body());
        entity.setStatus(PullRequestMapper.mapStatus(response.state(), response.merged()));
        entity.setHtmlUrl(response.htmlUrl());
        entity.setApiUrl(response.url());
        entity.setUpdatedAt(PullRequestMapper.parseTime(response.updatedAt()));
        entity.setClosedAt(PullRequestMapper.parseTime(response.closedAt()));
        entity.setMergedAt(PullRequestMapper.parseTime(response.mergedAt()));
    }

    /**
     * Executes a GitHub API call with retry logic.
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return operation.execute();
            } catch (GitHubApiException e) {
                lastException = e;
                // Don't retry on 4xx errors (client errors)
                if (e.getMessage().contains("404") || e.getMessage().contains("403") || e.getMessage().contains("401")) {
                    throw e;
                }
                log.warn("GitHub API call failed (attempt {}/{}): {}", attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
            } catch (Exception e) {
                lastException = e;
                log.warn("Operation failed (attempt {}/{}): {}", attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
            }

            if (attempt < MAX_RETRY_ATTEMPTS) {
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new GitHubApiException("Operation interrupted during retry", ie);
                }
            }
        }

        throw new GitHubApiException("Operation failed after " + MAX_RETRY_ATTEMPTS + " attempts", lastException);
    }

    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }
}

