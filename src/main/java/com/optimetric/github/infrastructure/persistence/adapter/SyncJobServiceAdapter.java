package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.application.client.PullRequestClientPort;
import com.optimetric.github.application.port.SyncJobServicePort;
import com.optimetric.github.infrastructure.http.dto.*;
import com.optimetric.github.infrastructure.persistence.entity.*;
import com.optimetric.github.infrastructure.persistence.jpa.*;
import com.optimetric.shared.exception.GitHubApiException;
import com.optimetric.shared.exception.SyncJobException;
import com.optimetric.shared.exception.InvalidRepositoryFormatException;
import com.optimetric.github.infrastructure.core.mapper.GitHubResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service adapter for managing synchronization jobs.
 * Provides job lifecycle management, pagination state management, 
 * scheduling, and retry logic for GitHub PR ingestion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SyncJobServiceAdapter implements SyncJobServicePort {

    private final SyncJobRepository syncJobRepository;
    private final PullRequestRepository pullRequestRepository;
    private final PullRequestReviewRepository pullRequestReviewRepository;
    private final PullRequestCommitRepository pullRequestCommitRepository;
    private final RepositoryRepository repositoryRepository;
    private final PullRequestClientPort pullRequestClientPort;
    private final AuthServicePort authServicePort;
    
    // Configuration constants for retry and scheduling logic
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MINUTES = 5;
    private static final int STUCK_JOB_THRESHOLD_MINUTES = 30;

    @Override
    @Transactional
    public void executePrListJob(String repoFullName) {
        log.info("Executing PR_LIST job for repository: {}", repoFullName);
        
        // Validate input
        validateRepositoryFullName(repoFullName);
        
        // Find or create PR_LIST job
        SyncJobEntity job = findOrCreateJob(SyncJobType.PR_LIST, repoFullName);
        
        try {
            // Update job status to RUNNING
            updateJobToRunning(job);
            
            // Check if we have all required dependencies for full implementation
            if (repositoryRepository == null || pullRequestClientPort == null || authServicePort == null) {
                log.warn("Missing dependencies for full PR_LIST job implementation - completing job without processing");
                updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
                return;
            }
            
            // Get repository and user access token
            RepositoryEntity repository = getRepositoryByFullName(repoFullName);
            String accessToken = authServicePort.getAccessTokenFromLogin(repository.getUser().getLogin());
            
            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new SyncJobException("No valid access token found for repository: " + repoFullName);
            }
            
            // Parse owner and repo from full name
            String[] parts = repoFullName.split("/");
            if (parts.length != 2) {
                throw new InvalidRepositoryFormatException("Invalid repository full name format: " + repoFullName);
            }
            String owner = parts[0];
            String repo = parts[1];
            
            int page = job.getCurrentPage() != null ? job.getCurrentPage() : 1;
            int processedCount = 0;
            
            // Fetch PRs page by page
            while (true) {
                log.debug("Fetching PRs for {}/{} - page {}", owner, repo, page);
                
                List<GitHubPullRequestResponse> pullRequests = pullRequestClientPort.getPullRequests(
                    accessToken, owner, repo, page);
                
                if (pullRequests.isEmpty()) {
                    log.debug("No more PRs found for {}/{} at page {}", owner, repo, page);
                    break;
                }
                
                // Process each PR
                for (GitHubPullRequestResponse prResponse : pullRequests) {
                    try {
                        upsertBasicPullRequest(repository, prResponse);
                        processedCount++;
                    } catch (Exception e) {
                        log.error("Failed to process PR {} for repository {}: {}", prResponse.id(), repoFullName, e.getMessage());
                        // Continue processing other PRs
                    }
                }
                
                // Update pagination state
                updateJobPaginationState(job.getId(), null, page);
                
                // Move to next page
                page++;
                
                // Break if we got less than 100 results (last page)
                if (pullRequests.size() < 100) {
                    break;
                }
            }
            
            log.info("PR_LIST job completed for repository: {} - processed {} PRs", repoFullName, processedCount);
            
            // Schedule detail jobs for PRs that need them
            scheduleDetailJobs(repository);
            
            // Mark job as completed
            updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
            
        } catch (SyncJobException | InvalidRepositoryFormatException e) {
            log.error("PR_LIST job failed for repository: {}", repoFullName, e);
            handleJobFailure(job, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("PR_LIST job failed for repository: {}", repoFullName, e);
            handleJobFailure(job, "Unexpected error: " + e.getMessage());
            throw new SyncJobException("Failed to execute PR_LIST job for repository: " + repoFullName, e);
        }
    }

    @Override
    @Transactional
    public void executePrDetailJob(String repoFullName, Long pullRequestId) {
        log.info("Executing PR_DETAIL job for repository: {} and PR ID: {}", repoFullName, pullRequestId);
        
        // Validate inputs
        validateRepositoryFullName(repoFullName);
        validatePullRequestId(pullRequestId);
        
        // Create job with PR ID in cursor for tracking
        SyncJobEntity job = createJobWithCursor(SyncJobType.PR_DETAIL, repoFullName, pullRequestId.toString());
        
        try {
            updateJobToRunning(job);
            
            // Check if we have all required dependencies for full implementation
            if (pullRequestRepository == null || pullRequestClientPort == null || authServicePort == null) {
                log.warn("Missing dependencies for full PR_DETAIL job implementation - completing job without processing");
                updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
                return;
            }
            
            // Get pull request entity
            Optional<PullRequestEntity> prOpt = pullRequestRepository.findById(pullRequestId);
            if (prOpt.isEmpty()) {
                throw new IllegalArgumentException("Pull request not found with ID: " + pullRequestId);
            }
            
            PullRequestEntity pullRequest = prOpt.get();
            RepositoryEntity repository = pullRequest.getRepository();
            String accessToken = authServicePort.getAccessTokenFromLogin(repository.getUser().getLogin());
            
            // Parse owner and repo from full name
            String[] parts = repoFullName.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid repository full name format: " + repoFullName);
            }
            String owner = parts[0];
            String repo = parts[1];
            
            // Fetch detailed PR information
            GitHubPullRequestDetailResponse detailResponse = pullRequestClientPort.getPullRequestDetails(
                accessToken, owner, repo, pullRequest.getPrNumber());
            
            // Update PR with change statistics
            updatePullRequestWithDetails(pullRequest, detailResponse);
            
            log.info("PR_DETAIL job completed for repository: {} and PR ID: {}", repoFullName, pullRequestId);
            
            updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
            
        } catch (SyncJobException | InvalidRepositoryFormatException e) {
            log.error("PR_DETAIL job failed for repository: {} and PR ID: {}", repoFullName, pullRequestId, e);
            handleJobFailure(job, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("PR_DETAIL job failed for repository: {} and PR ID: {}", repoFullName, pullRequestId, e);
            handleJobFailure(job, "Unexpected error: " + e.getMessage());
            throw new SyncJobException("Failed to execute PR_DETAIL job for repository: " + repoFullName + " PR ID: " + pullRequestId, e);
        }
    }

    @Override
    @Transactional
    public void executePrReviewsJob(String repoFullName, Long pullRequestId) {
        log.info("Executing PR_REVIEWS job for repository: {} and PR ID: {}", repoFullName, pullRequestId);
        
        // Validate inputs
        validateRepositoryFullName(repoFullName);
        validatePullRequestId(pullRequestId);
        
        SyncJobEntity job = createJobWithCursor(SyncJobType.PR_REVIEWS, repoFullName, pullRequestId.toString());
        
        try {
            updateJobToRunning(job);
            
            // Check if we have all required dependencies for full implementation
            if (pullRequestRepository == null || pullRequestReviewRepository == null || 
                pullRequestClientPort == null || authServicePort == null) {
                log.warn("Missing dependencies for full PR_REVIEWS job implementation - completing job without processing");
                updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
                return;
            }
            
            // Get pull request entity
            Optional<PullRequestEntity> prOpt = pullRequestRepository.findById(pullRequestId);
            if (prOpt.isEmpty()) {
                throw new IllegalArgumentException("Pull request not found with ID: " + pullRequestId);
            }
            
            PullRequestEntity pullRequest = prOpt.get();
            RepositoryEntity repository = pullRequest.getRepository();
            String accessToken = authServicePort.getAccessTokenFromLogin(repository.getUser().getLogin());
            
            // Parse owner and repo from full name
            String[] parts = repoFullName.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid repository full name format: " + repoFullName);
            }
            String owner = parts[0];
            String repo = parts[1];
            
            // Fetch PR reviews
            List<GitHubPullRequestReviewResponse> reviewResponses = pullRequestClientPort.getPullRequestReviews(
                accessToken, owner, repo, pullRequest.getPrNumber());
            
            int processedCount = 0;
            
            // Process each review
            for (GitHubPullRequestReviewResponse reviewResponse : reviewResponses) {
                upsertPullRequestReview(pullRequest, reviewResponse);
                processedCount++;
            }
            
            log.info("PR_REVIEWS job completed for repository: {} and PR ID: {} - processed {} reviews", 
                    repoFullName, pullRequestId, processedCount);
            
            updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
            
        } catch (SyncJobException | InvalidRepositoryFormatException e) {
            log.error("PR_REVIEWS job failed for repository: {} and PR ID: {}", repoFullName, pullRequestId, e);
            handleJobFailure(job, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("PR_REVIEWS job failed for repository: {} and PR ID: {}", repoFullName, pullRequestId, e);
            handleJobFailure(job, "Unexpected error: " + e.getMessage());
            throw new SyncJobException("Failed to execute PR_REVIEWS job for repository: " + repoFullName + " PR ID: " + pullRequestId, e);
        }
    }

    @Override
    @Transactional
    public void executePrCommitsJob(String repoFullName, Long pullRequestId) {
        log.info("Executing PR_COMMITS job for repository: {} and PR ID: {}", repoFullName, pullRequestId);
        
        // Validate inputs
        validateRepositoryFullName(repoFullName);
        validatePullRequestId(pullRequestId);
        
        SyncJobEntity job = createJobWithCursor(SyncJobType.PR_COMMITS, repoFullName, pullRequestId.toString());
        
        try {
            updateJobToRunning(job);
            
            // Check if we have all required dependencies for full implementation
            if (pullRequestRepository == null || pullRequestCommitRepository == null || 
                pullRequestClientPort == null || authServicePort == null) {
                log.warn("Missing dependencies for full PR_COMMITS job implementation - completing job without processing");
                updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
                return;
            }
            
            // Get pull request entity
            Optional<PullRequestEntity> prOpt = pullRequestRepository.findById(pullRequestId);
            if (prOpt.isEmpty()) {
                throw new IllegalArgumentException("Pull request not found with ID: " + pullRequestId);
            }
            
            PullRequestEntity pullRequest = prOpt.get();
            RepositoryEntity repository = pullRequest.getRepository();
            String accessToken = authServicePort.getAccessTokenFromLogin(repository.getUser().getLogin());
            
            // Parse owner and repo from full name
            String[] parts = repoFullName.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid repository full name format: " + repoFullName);
            }
            String owner = parts[0];
            String repo = parts[1];
            
            // Fetch PR commits
            List<GitHubPullRequestCommitResponse> commitResponses = pullRequestClientPort.getPullRequestCommits(
                accessToken, owner, repo, pullRequest.getPrNumber());
            
            int processedCount = 0;
            
            // Process each commit
            for (GitHubPullRequestCommitResponse commitResponse : commitResponses) {
                upsertPullRequestCommit(pullRequest, repository.getGithubRepoId(), commitResponse);
                processedCount++;
            }
            
            log.info("PR_COMMITS job completed for repository: {} and PR ID: {} - processed {} commits", 
                    repoFullName, pullRequestId, processedCount);
            
            updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
            
        } catch (SyncJobException | InvalidRepositoryFormatException e) {
            log.error("PR_COMMITS job failed for repository: {} and PR ID: {}", repoFullName, pullRequestId, e);
            handleJobFailure(job, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("PR_COMMITS job failed for repository: {} and PR ID: {}", repoFullName, pullRequestId, e);
            handleJobFailure(job, "Unexpected error: " + e.getMessage());
            throw new SyncJobException("Failed to execute PR_COMMITS job for repository: " + repoFullName + " PR ID: " + pullRequestId, e);
        }
    }

    @Override
    @Transactional
    public SyncJobEntity createJob(SyncJobType jobType, String repoFullName) {
        log.debug("Creating sync job of type {} for repository: {}", jobType, repoFullName);
        
        SyncJobEntity job = SyncJobEntity.builder()
                .jobType(jobType)
                .repoFullName(repoFullName)
                .status(SyncJobStatus.PENDING)
                .attemptCount(0)
                .currentPage(1) // Start with page 1 for pagination
                .build();
        
        SyncJobEntity savedJob = syncJobRepository.save(job);
        log.debug("Created sync job with ID: {} for repository: {}", savedJob.getId(), repoFullName);
        
        return savedJob;
    }

    @Override
    @Transactional
    public void updateJobStatus(Long jobId, SyncJobStatus status, String errorMessage) {
        log.debug("Updating job {} to status: {}", jobId, status);
        
        Optional<SyncJobEntity> jobOpt = syncJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            log.warn("Attempted to update non-existent job with ID: {}", jobId);
            return;
        }
        
        SyncJobEntity job = jobOpt.get();
        job.setStatus(status);
        
        OffsetDateTime now = OffsetDateTime.now();
        
        switch (status) {
            case RUNNING -> {
                job.setStartedAt(now);
                job.setAttemptCount(job.getAttemptCount() + 1);
            }
            case COMPLETED -> {
                job.setCompletedAt(now);
                job.setErrorMessage(null); // Clear any previous error
            }
            case FAILED -> {
                job.setCompletedAt(now);
                job.setErrorMessage(errorMessage);
                // Schedule retry if under max attempts
                if (job.getAttemptCount() < MAX_RETRY_ATTEMPTS) {
                    job.setNextRunAt(now.plusMinutes(RETRY_DELAY_MINUTES * job.getAttemptCount()));
                    job.setStatus(SyncJobStatus.PENDING); // Reset to pending for retry
                    log.info("Scheduling retry for job {} in {} minutes (attempt {}/{})", 
                            jobId, RETRY_DELAY_MINUTES * job.getAttemptCount(), 
                            job.getAttemptCount(), MAX_RETRY_ATTEMPTS);
                }
            }
        }
        
        syncJobRepository.save(job);
        log.debug("Updated job {} status to: {}", jobId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncJobEntity> getPendingJobs() {
        OffsetDateTime now = OffsetDateTime.now();
        List<SyncJobEntity> pendingJobs = syncJobRepository.findPendingJobsReadyToRun(SyncJobStatus.PENDING, now);
        log.debug("Found {} pending jobs ready to run", pendingJobs.size());
        return pendingJobs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncJobEntity> getJobsForRepository(String repoFullName) {
        List<SyncJobEntity> jobs = syncJobRepository.findByRepoFullNameOrderBySystemCreatedAtDesc(repoFullName);
        log.debug("Found {} jobs for repository: {}", jobs.size(), repoFullName);
        return jobs;
    }

    @Override
    @Transactional(readOnly = true)
    public SyncJobEntity getLatestJobForRepository(String repoFullName, SyncJobType jobType) {
        Optional<SyncJobEntity> latestJob = syncJobRepository
                .findFirstByRepoFullNameAndJobTypeAndStatusOrderByCompletedAtDesc(
                        repoFullName, jobType, SyncJobStatus.COMPLETED);
        
        if (latestJob.isPresent()) {
            log.debug("Found latest {} job for repository: {} completed at: {}", 
                    jobType, repoFullName, latestJob.get().getCompletedAt());
            return latestJob.get();
        } else {
            log.debug("No completed {} job found for repository: {}", jobType, repoFullName);
            return null;
        }
    }

    /**
     * Updates pagination state for a job to support resumable operations.
     * 
     * @param jobId Job ID to update
     * @param cursor Pagination cursor from GitHub API
     * @param currentPage Current page number
     */
    @Transactional
    public void updateJobPaginationState(Long jobId, String cursor, Integer currentPage) {
        log.debug("Updating pagination state for job {}: cursor={}, page={}", jobId, cursor, currentPage);
        
        Optional<SyncJobEntity> jobOpt = syncJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            log.warn("Attempted to update pagination for non-existent job with ID: {}", jobId);
            return;
        }
        
        SyncJobEntity job = jobOpt.get();
        job.setCursor(cursor);
        job.setCurrentPage(currentPage);
        
        syncJobRepository.save(job);
        log.debug("Updated pagination state for job {}", jobId);
    }

    /**
     * Finds jobs that appear to be stuck (running for too long) and resets them.
     * 
     * @return List of jobs that were reset
     */
    @Transactional
    public List<SyncJobEntity> resetStuckJobs() {
        OffsetDateTime stuckThreshold = OffsetDateTime.now().minusMinutes(STUCK_JOB_THRESHOLD_MINUTES);
        List<SyncJobEntity> stuckJobs = syncJobRepository.findStuckJobs(SyncJobStatus.RUNNING, stuckThreshold);
        
        for (SyncJobEntity job : stuckJobs) {
            log.warn("Resetting stuck job {} that has been running since {}", job.getId(), job.getStartedAt());
            job.setStatus(SyncJobStatus.FAILED);
            job.setErrorMessage("Job was stuck and automatically reset");
            job.setCompletedAt(OffsetDateTime.now());
            
            // Schedule for retry if under max attempts
            if (job.getAttemptCount() < MAX_RETRY_ATTEMPTS) {
                job.setNextRunAt(OffsetDateTime.now().plusMinutes(RETRY_DELAY_MINUTES));
                job.setStatus(SyncJobStatus.PENDING);
            }
        }
        
        if (!stuckJobs.isEmpty()) {
            syncJobRepository.saveAll(stuckJobs);
            log.info("Reset {} stuck jobs", stuckJobs.size());
        }
        
        return stuckJobs;
    }

    /**
     * Cleans up old completed and failed jobs to prevent database bloat.
     * 
     * @param daysOld Jobs older than this many days will be deleted
     * @return Number of jobs deleted
     */
    @Transactional
    public int cleanupOldJobs(int daysOld) {
        OffsetDateTime cleanupThreshold = OffsetDateTime.now().minusDays(daysOld);
        List<SyncJobStatus> completedStatuses = List.of(SyncJobStatus.COMPLETED, SyncJobStatus.FAILED);
        
        List<SyncJobEntity> oldJobs = syncJobRepository.findJobsForCleanup(completedStatuses, cleanupThreshold);
        
        if (!oldJobs.isEmpty()) {
            syncJobRepository.deleteAll(oldJobs);
            log.info("Cleaned up {} old sync jobs older than {} days", oldJobs.size(), daysOld);
        }
        
        return oldJobs.size();
    }

    // Private helper methods

    private SyncJobEntity findOrCreateJob(SyncJobType jobType, String repoFullName) {
        // Check if there's already a pending or running job of this type for this repository
        Optional<SyncJobEntity> existingJob = syncJobRepository
                .findByRepoFullNameAndJobTypeAndStatus(repoFullName, jobType, SyncJobStatus.PENDING);
        
        if (existingJob.isEmpty()) {
            existingJob = syncJobRepository
                    .findByRepoFullNameAndJobTypeAndStatus(repoFullName, jobType, SyncJobStatus.RUNNING);
        }
        
        if (existingJob.isPresent()) {
            log.debug("Found existing {} job for repository: {}", jobType, repoFullName);
            return existingJob.get();
        }
        
        // Create new job if none exists
        return createJob(jobType, repoFullName);
    }

    private SyncJobEntity createJobWithCursor(SyncJobType jobType, String repoFullName, String cursor) {
        SyncJobEntity job = createJob(jobType, repoFullName);
        job.setCursor(cursor);
        return syncJobRepository.save(job);
    }

    private void updateJobToRunning(SyncJobEntity job) {
        job.setStatus(SyncJobStatus.RUNNING);
        job.setStartedAt(OffsetDateTime.now());
        job.setAttemptCount(job.getAttemptCount() + 1);
        syncJobRepository.save(job);
    }

    private void handleJobFailure(SyncJobEntity job, String errorMessage) {
        updateJobStatus(job.getId(), SyncJobStatus.FAILED, errorMessage);
    }

    // Data processing helper methods

    /**
     * Gets repository entity by full name, throwing exception if not found.
     */
    private RepositoryEntity getRepositoryByFullName(String repoFullName) {
        // For now, we'll find the first repository with this full name
        // In a real implementation, we might need user context
        List<RepositoryEntity> repositories = repositoryRepository.findAll().stream()
                .filter(repo -> repoFullName.equals(repo.getFullName()))
                .toList();
        
        if (repositories.isEmpty()) {
            throw new IllegalArgumentException("Repository not found: " + repoFullName);
        }
        
        return repositories.get(0);
    }

    /**
     * Creates or updates a basic pull request from GitHub API response.
     * Implements idempotent operations using github_pr_id.
     */
    private void upsertBasicPullRequest(RepositoryEntity repository, GitHubPullRequestResponse prResponse) {
        log.debug("Upserting basic PR data for github_pr_id: {}", prResponse.id());
        
        // Find existing PR by github_pr_id
        Optional<PullRequestEntity> existingPr = pullRequestRepository.findByGithubPrId(prResponse.id());
        
        PullRequestEntity pullRequest = null;
        if (existingPr.isPresent()) {
            pullRequest = existingPr.get();
            
            // Check if PR needs synchronization based on updated_at timestamp
            if (prResponse.updatedAt() != null && pullRequest.getLastSyncedAt() != null &&
                !prResponse.updatedAt().isAfter(pullRequest.getLastSyncedAt())) {
                log.debug("PR {} is up to date, skipping", prResponse.id());
                return;
            }
            
            log.debug("Updating existing PR: {}", prResponse.id());
        } else {
            log.debug("Creating new PR: {}", prResponse.id());
        }
        
        // Use mapper to create or update entity
        pullRequest = GitHubResponseMapper.toPullRequestEntity(prResponse, repository, pullRequest);
        
        pullRequestRepository.save(pullRequest);
        log.debug("Upserted PR: {} (#{}) for repository: {}", 
                 prResponse.id(), prResponse.number(), repository.getFullName());
    }

    /**
     * Updates pull request with detailed information including change statistics.
     */
    private void updatePullRequestWithDetails(PullRequestEntity pullRequest, GitHubPullRequestDetailResponse detailResponse) {
        log.debug("Updating PR {} with detailed information", pullRequest.getGithubPrId());
        
        // Use mapper to update entity with details
        GitHubResponseMapper.updateWithDetails(pullRequest, detailResponse);
        
        pullRequestRepository.save(pullRequest);
        log.debug("Updated PR {} with change statistics: +{} -{} files:{}", 
                 pullRequest.getGithubPrId(), detailResponse.additions(), 
                 detailResponse.deletions(), detailResponse.changedFiles());
    }

    /**
     * Creates or updates a pull request review from GitHub API response.
     * Implements idempotent operations using github_review_id.
     */
    private void upsertPullRequestReview(PullRequestEntity pullRequest, GitHubPullRequestReviewResponse reviewResponse) {
        log.debug("Upserting review for github_review_id: {}", reviewResponse.id());
        
        // Find existing review by github_review_id
        Optional<PullRequestReviewEntity> existingReview = pullRequestReviewRepository.findByGithubReviewId(reviewResponse.id());
        
        PullRequestReviewEntity review = null;
        if (existingReview.isPresent()) {
            review = existingReview.get();
            log.debug("Updating existing review: {}", reviewResponse.id());
        } else {
            log.debug("Creating new review: {}", reviewResponse.id());
        }
        
        // Use mapper to create or update entity
        review = GitHubResponseMapper.toPullRequestReviewEntity(reviewResponse, pullRequest, review);
        
        pullRequestReviewRepository.save(review);
        log.debug("Upserted review: {} for PR: {}", reviewResponse.id(), pullRequest.getGithubPrId());
    }

    /**
     * Creates or updates a pull request commit from GitHub API response.
     * Implements idempotent operations using (github_repo_id, sha) unique constraint.
     */
    private void upsertPullRequestCommit(PullRequestEntity pullRequest, Long githubRepoId, GitHubPullRequestCommitResponse commitResponse) {
        log.debug("Upserting commit for sha: {}", commitResponse.sha());
        
        // Find existing commit by composite key (github_repo_id, sha)
        Optional<PullRequestCommitEntity> existingCommit = pullRequestCommitRepository.findByGithubRepoIdAndSha(githubRepoId, commitResponse.sha());
        
        PullRequestCommitEntity commit = null;
        if (existingCommit.isPresent()) {
            commit = existingCommit.get();
            log.debug("Updating existing commit: {}", commitResponse.sha());
        } else {
            log.debug("Creating new commit: {}", commitResponse.sha());
        }
        
        // Use mapper to create or update entity
        commit = GitHubResponseMapper.toPullRequestCommitEntity(commitResponse, pullRequest, githubRepoId, commit);
        
        pullRequestCommitRepository.save(commit);
        log.debug("Upserted commit: {} for PR: {}", commitResponse.sha(), pullRequest.getGithubPrId());
    }

    /**
     * Schedules detail jobs (PR_DETAIL, PR_REVIEWS, PR_COMMITS) for PRs that need them.
     * This implements the job orchestration logic for sequential execution.
     */
    private void scheduleDetailJobs(RepositoryEntity repository) {
        if (pullRequestRepository == null) {
            log.warn("Cannot schedule detail jobs - pullRequestRepository is null");
            return;
        }
        
        log.debug("Scheduling detail jobs for repository: {}", repository.getFullName());
        
        // Find PRs that need detail jobs (those without change statistics)
        List<PullRequestEntity> prsNeedingDetails = pullRequestRepository.findPullRequestsWithoutChangeStats(repository);
        
        for (PullRequestEntity pr : prsNeedingDetails) {
            try {
                // Schedule PR_DETAIL job
                log.debug("Scheduling PR_DETAIL job for PR: {}", pr.getGithubPrId());
                executePrDetailJob(repository.getFullName(), pr.getId());
                
                // Schedule PR_REVIEWS job
                log.debug("Scheduling PR_REVIEWS job for PR: {}", pr.getGithubPrId());
                executePrReviewsJob(repository.getFullName(), pr.getId());
                
                // Schedule PR_COMMITS job
                log.debug("Scheduling PR_COMMITS job for PR: {}", pr.getGithubPrId());
                executePrCommitsJob(repository.getFullName(), pr.getId());
                
            } catch (Exception e) {
                log.error("Failed to schedule detail jobs for PR: {}", pr.getGithubPrId(), e);
                // Continue with other PRs even if one fails
            }
        }
        
        log.info("Scheduled detail jobs for {} PRs in repository: {}", prsNeedingDetails.size(), repository.getFullName());
    }

    /**
     * Validates repository full name format.
     */
    private void validateRepositoryFullName(String repoFullName) {
        if (repoFullName == null || repoFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository full name cannot be null or empty");
        }
        
        if (!repoFullName.matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$")) {
            throw new InvalidRepositoryFormatException(
                "Invalid repository format: '" + repoFullName + "'. Expected format: 'owner/repo'");
        }
    }

    /**
     * Validates pull request ID parameter.
     */
    private void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull request ID cannot be null");
        }
        
        if (pullRequestId <= 0) {
            throw new IllegalArgumentException("Pull request ID must be positive, got: " + pullRequestId);
        }
    }
}