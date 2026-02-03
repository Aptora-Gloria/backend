package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.auth.infrastructure.persistence.jpa.repository.UserRepository;
import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.dto.ReviewCommentInfo;
import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.github.application.dto.SyncStatusInfo;
import com.optimetric.github.application.port.out.PullRequestQueryPort;
import com.optimetric.github.application.port.SyncJobServicePort;
import com.optimetric.github.infrastructure.persistence.entity.DataLevel;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobStatus;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobType;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestCommitRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestReviewRepository;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.github.infrastructure.core.mapper.PullRequestMapper;
import com.optimetric.github.infrastructure.core.mapper.SyncJobMapper;
import com.optimetric.shared.exception.InvalidRepositoryFormatException;
import com.optimetric.shared.exception.PullRequestNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing PullRequestQueryPort (outbound).
 * Use cases call this port; returns Application DTO.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PullRequestServiceAdapter implements PullRequestQueryPort {

    private final PullRequestRepository pullRequestRepository;
    private final PullRequestReviewRepository pullRequestReviewRepository;
    private final PullRequestCommitRepository pullRequestCommitRepository;
    private final RepositoryRepository repositoryRepository;
    private final UserRepository userRepository;
    private final AuthServicePort authServicePort;
    private final SyncJobServicePort syncJobServicePort;

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestInfo> getPullRequestsForRepository(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException {
        
        log.debug("Getting pull requests for repository: {} by user: {}", repoFullName, login);
        validateInputParameters(login, repoFullName);
        RepositoryEntity repository = validateRepositoryAccess(login, repoFullName);
        
        try {
            List<PullRequestEntity> pullRequests = pullRequestRepository.findByRepositoryOrderByCreatedAtDesc(repository);
            log.debug("Found {} pull requests for repository: {}", pullRequests.size(), repoFullName);
            return PullRequestMapper.toPullRequestInfos(pullRequests);
        } catch (Exception e) {
            log.error("Failed to retrieve pull requests for repository: {} by user: {}", repoFullName, login, e);
            throw new RuntimeException("Failed to retrieve pull requests: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PullRequestInfo getPullRequest(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException {
        
        log.debug("Getting pull request #{} for repository: {} by user: {}", prNumber, repoFullName, login);
        validateInputParameters(login, repoFullName);
        validatePrNumber(prNumber);
        RepositoryEntity repository = validateRepositoryAccess(login, repoFullName);
        
        try {
            Optional<PullRequestEntity> pullRequest = pullRequestRepository.findByRepositoryAndPrNumber(repository, prNumber);
            if (pullRequest.isEmpty()) {
                log.warn("Pull request #{} not found for repository: {}", prNumber, repoFullName);
                throw new PullRequestNotFoundException("Pull request #" + prNumber + " not found for repository: " + repoFullName);
            }
            return PullRequestMapper.toPullRequestInfo(pullRequest.get());
        } catch (PullRequestNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve pull request #{} for repository: {} by user: {}", prNumber, repoFullName, login, e);
            throw new RuntimeException("Failed to retrieve pull request: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewInfo> getPullRequestReviews(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException {
        
        log.debug("Getting reviews for pull request #{} in repository: {} by user: {}", prNumber, repoFullName, login);
        
        // Validate input parameters
        validateInputParameters(login, repoFullName);
        validatePrNumber(prNumber);
        
        // Validate user and repository access
        RepositoryEntity repository = validateRepositoryAccess(login, repoFullName);
        
        try {
            // Find the pull request
            Optional<PullRequestEntity> pullRequest = pullRequestRepository.findByRepositoryAndPrNumber(repository, prNumber);
            
            if (pullRequest.isEmpty()) {
                log.warn("Pull request #{} not found for repository: {}", prNumber, repoFullName);
                throw new PullRequestNotFoundException("Pull request #" + prNumber + " not found for repository: " + repoFullName);
            }
            
            // Get reviews for the pull request
            List<PullRequestReviewEntity> reviews = pullRequestReviewRepository.findByPullRequestOrderBySubmittedAtAsc(pullRequest.get());
            log.debug("Found {} reviews for pull request #{}", reviews.size(), prNumber);
            return PullRequestMapper.toReviewInfos(reviews);
        } catch (PullRequestNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve reviews for pull request #{} in repository: {} by user: {}", prNumber, repoFullName, login, e);
            throw new RuntimeException("Failed to retrieve pull request reviews: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewCommentInfo> getPullRequestCommits(String login, String repoFullName, Integer prNumber)
            throws UserNotFoundException, RepositoryNotFoundException {
        
        log.debug("Getting commits for pull request #{} in repository: {} by user: {}", prNumber, repoFullName, login);
        validateInputParameters(login, repoFullName);
        validatePrNumber(prNumber);
        RepositoryEntity repository = validateRepositoryAccess(login, repoFullName);
        
        try {
            Optional<PullRequestEntity> pullRequest = pullRequestRepository.findByRepositoryAndPrNumber(repository, prNumber);
            if (pullRequest.isEmpty()) {
                log.warn("Pull request #{} not found for repository: {}", prNumber, repoFullName);
                throw new PullRequestNotFoundException("Pull request #" + prNumber + " not found for repository: " + repoFullName);
            }
            List<PullRequestCommitEntity> commits = pullRequestCommitRepository.findByPullRequestOrderByCommittedAtAsc(pullRequest.get());
            log.debug("Found {} commits for pull request #{}", commits.size(), prNumber);
            return PullRequestMapper.toReviewCommentInfos(commits);
        } catch (PullRequestNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve commits for pull request #{} in repository: {} by user: {}", prNumber, repoFullName, login, e);
            throw new RuntimeException("Failed to retrieve pull request commits: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void initiatePullRequestSync(String login, String repoFullName) 
            throws UserNotFoundException, RepositoryNotFoundException {
        
        log.info("Initiating pull request sync for repository: {} by user: {}", repoFullName, login);
        
        // Validate input parameters
        validateInputParameters(login, repoFullName);
        
        // Validate user and repository access
        RepositoryEntity repository = validateRepositoryAccess(login, repoFullName);
        
        try {
            // Check if there's already a running sync job for this repository
            List<SyncJobEntity> runningJobs = syncJobServicePort.getJobsForRepository(repoFullName)
                    .stream()
                    .filter(job -> job.getStatus() == SyncJobStatus.RUNNING || job.getStatus() == SyncJobStatus.PENDING)
                    .toList();
            
            if (!runningJobs.isEmpty()) {
                log.warn("Sync already in progress for repository: {} - found {} active jobs", repoFullName, runningJobs.size());
                throw new IllegalStateException("Synchronization already in progress for repository: " + repoFullName);
            }
            
            // Create PR_LIST job to start the synchronization pipeline
            SyncJobEntity job = syncJobServicePort.createJob(SyncJobType.PR_LIST, repoFullName);
            
            log.info("Created sync job {} for repository: {}", job.getId(), repoFullName);
            
            // Execute the job immediately (in a real system, this might be queued)
            syncJobServicePort.executePrListJob(repoFullName);
            log.info("Successfully initiated sync for repository: {}", repoFullName);
            
        } catch (IllegalStateException e) {
            throw e; // Re-throw specific exception
        } catch (Exception e) {
            log.error("Failed to initiate sync for repository: {} by user: {}", repoFullName, login, e);
            throw new RuntimeException("Failed to initiate pull request synchronization: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SyncStatusInfo getSyncStatus(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException {
        
        log.debug("Getting sync status for repository: {} by user: {}", repoFullName, login);
        validateInputParameters(login, repoFullName);
        validateRepositoryAccess(login, repoFullName);
        
        try {
            List<SyncJobEntity> jobs = syncJobServicePort.getJobsForRepository(repoFullName);
            SyncJobEntity latestPrListJob = syncJobServicePort.getLatestJobForRepository(repoFullName, SyncJobType.PR_LIST);
            SyncJobEntity latestPrDetailJob = syncJobServicePort.getLatestJobForRepository(repoFullName, SyncJobType.PR_DETAIL);
            SyncJobEntity latestPrReviewsJob = syncJobServicePort.getLatestJobForRepository(repoFullName, SyncJobType.PR_REVIEWS);
            SyncJobEntity latestPrCommitsJob = syncJobServicePort.getLatestJobForRepository(repoFullName, SyncJobType.PR_COMMITS);
            
            com.optimetric.github.domain.enumeration.SyncJobStatus overallStatus =
                    SyncJobMapper.determineOverallSyncStatus(latestPrListJob, latestPrDetailJob, latestPrReviewsJob, latestPrCommitsJob);
            OffsetDateTime lastSyncTime = SyncJobMapper.getLastSyncTime(latestPrListJob, latestPrDetailJob, latestPrReviewsJob, latestPrCommitsJob);
            return SyncJobMapper.toSyncStatusInfo(repoFullName, overallStatus, jobs, lastSyncTime);
        } catch (Exception e) {
            log.error("Failed to get sync status for repository: {} by user: {}", repoFullName, login, e);
            throw new RuntimeException("Failed to retrieve synchronization status: " + e.getMessage(), e);
        }
    }

    /**
     * Validates that the user has access to the specified repository.
     * Follows existing repository access validation patterns.
     */
    private RepositoryEntity validateRepositoryAccess(String login, String repoFullName) 
            throws UserNotFoundException, RepositoryNotFoundException {
        
        // Get user info (Application DTO)
        com.optimetric.auth.application.dto.UserInfo userInfo = authServicePort.findByLoginOrThrow(login);
        
        // Resolve User entity from userId (infrastructure concern - needed for JPA repository)
        User user = userRepository.findById(userInfo.id())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userInfo.id()));
        
        // Find repository by full name and user (following existing patterns)
        Optional<RepositoryEntity> repository = repositoryRepository.findByFullNameAndUser(repoFullName, user);
        
        if (repository.isEmpty()) {
            log.warn("Repository {} not found or not accessible by user: {}", repoFullName, login);
            throw new RepositoryNotFoundException("Repository not found or not accessible: " + repoFullName);
        }
        
        return repository.get();
    }

    /**
     * Checks if a pull request needs synchronization based on timestamp comparison.
     * Implements incremental sync logic as per requirements 3.1, 3.2, 3.3.
     */
    public boolean needsSynchronization(PullRequestEntity pullRequest) {
        if (pullRequest == null) {
            return false;
        }
        
        // If never synced, needs sync
        if (pullRequest.getLastSyncedAt() == null) {
            return true;
        }
        
        // If GitHub updated_at is newer than last_synced_at, needs sync
        if (pullRequest.getUpdatedAt() != null && 
            pullRequest.getUpdatedAt().isAfter(pullRequest.getLastSyncedAt())) {
            return true;
        }
        
        return false;
    }

    /**
     * Updates data level based on completeness of PR data.
     * Implements data level transition logic as per requirements 7.3, 7.4.
     */
    @Transactional
    public void updateDataLevel(PullRequestEntity pullRequest) {
        if (pullRequest == null) {
            return;
        }
        
        // Check if all detail data is available
        boolean hasChangeStats = pullRequest.getAdditions() != null && 
                                pullRequest.getDeletions() != null && 
                                pullRequest.getChangedFiles() != null;
        
        // For now, we consider FULL when change stats are available
        // In a complete implementation, we would also check for reviews and commits
        DataLevel newDataLevel = hasChangeStats ? DataLevel.FULL : DataLevel.BASIC;
        
        if (pullRequest.getDataLevel() != newDataLevel) {
            log.debug("Updating data level for PR {} from {} to {}", 
                     pullRequest.getGithubPrId(), pullRequest.getDataLevel(), newDataLevel);
            
            pullRequest.setDataLevel(newDataLevel);
            pullRequestRepository.save(pullRequest);
        }
    }

    /**
     * Updates the last synced timestamp for a pull request.
     * Called after successful synchronization operations.
     */
    @Transactional
    public void updateLastSyncedAt(PullRequestEntity pullRequest) {
        if (pullRequest != null) {
            pullRequest.setLastSyncedAt(OffsetDateTime.now());
            pullRequestRepository.save(pullRequest);
        }
    }

    /**
     * Validates input parameters for all service methods.
     */
    private void validateInputParameters(String login, String repoFullName) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("User login cannot be null or empty");
        }
        
        if (repoFullName == null || repoFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository full name cannot be null or empty");
        }
        
        // Validate repository full name format (owner/repo)
        if (!repoFullName.matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$")) {
            throw new InvalidRepositoryFormatException(
                "Invalid repository format: '" + repoFullName + "'. Expected format: 'owner/repo'");
        }
    }

    /**
     * Validates pull request number parameter.
     */
    private void validatePrNumber(Integer prNumber) {
        if (prNumber == null) {
            throw new IllegalArgumentException("Pull request number cannot be null");
        }
        
        if (prNumber <= 0) {
            throw new IllegalArgumentException("Pull request number must be positive, got: " + prNumber);
        }
    }
}
