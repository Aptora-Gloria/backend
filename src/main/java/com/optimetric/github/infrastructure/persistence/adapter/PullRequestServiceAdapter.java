package com.optimetric.github.infrastructure.persistence.adapter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.dto.ReviewCommentInfo;
import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.github.application.dto.SyncStatusInfo;
import com.optimetric.github.application.dto.SyncJobInfo;
import com.optimetric.github.application.port.SyncJobServicePort;
import com.optimetric.github.application.port.out.PullRequestQueryPort;
import com.optimetric.github.infrastructure.core.mapper.PullRequestMapper;
import com.optimetric.github.infrastructure.core.mapper.SyncJobMapper;
import com.optimetric.github.domain.enumeration.DataLevel;
import com.optimetric.github.domain.service.DataLevelPolicy;
import com.optimetric.github.domain.service.PullRequestSyncPolicy;
import com.optimetric.github.domain.service.SyncJobPolicy;
import com.optimetric.github.domain.vo.SyncJobSnapshot;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestCommitRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestReviewRepository;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.shared.exception.InvalidRepositoryFormatException;
import com.optimetric.shared.exception.PullRequestNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// PullRequestQueryPort (giden) uygulayan adapter; use case çağırır, Application DTO döner.
@Service
@RequiredArgsConstructor
@Slf4j
public class PullRequestServiceAdapter implements PullRequestQueryPort {

    private final PullRequestRepository pullRequestRepository;
    private final PullRequestReviewRepository pullRequestReviewRepository;
    private final PullRequestCommitRepository pullRequestCommitRepository;
    private final RepositoryRepository repositoryRepository;
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
        
        // Kullanıcı ve repo erişimini doğrula
        RepositoryEntity repository = validateRepositoryAccess(login, repoFullName);
        
        try {
            // Pull request'i bul
            Optional<PullRequestEntity> pullRequest = pullRequestRepository.findByRepositoryAndPrNumber(repository, prNumber);
            
            if (pullRequest.isEmpty()) {
                log.warn("Pull request #{} not found for repository: {}", prNumber, repoFullName);
                throw new PullRequestNotFoundException("Pull request #" + prNumber + " not found for repository: " + repoFullName);
            }
            
            // PR için incelemeleri al
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
        
        // Kullanıcı ve repo erişimini doğrula
        RepositoryEntity repository = validateRepositoryAccess(login, repoFullName);
        
        try {
            List<SyncJobInfo> jobsForRepo = syncJobServicePort.getJobsForRepository(repoFullName);
            List<SyncJobSnapshot> snapshots = jobsForRepo.stream().map(SyncJobInfo::toSnapshot).toList();
            if (!SyncJobPolicy.canStartSync(snapshots)) {
                log.warn("Sync already in progress for repository: {} - active jobs present", repoFullName);
                throw new IllegalStateException("Synchronization already in progress for repository: " + repoFullName);
            }

            // Senkronizasyonu başlatmak için PR_LIST job oluştur
            SyncJobInfo job = syncJobServicePort.createJob(SyncJobType.PR_LIST, repoFullName);

            log.info("Created sync job {} for repository: {}", job.id(), repoFullName);
            
            // Job'u hemen çalıştır (gerçek sistemde kuyruğa alınabilir)
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
            List<SyncJobInfo> jobs = syncJobServicePort.getJobsForRepository(repoFullName);
            SyncJobInfo latestPrListJob = syncJobServicePort.getLatestJobForRepository(repoFullName, SyncJobType.PR_LIST);
            SyncJobInfo latestPrDetailJob = syncJobServicePort.getLatestJobForRepository(repoFullName, SyncJobType.PR_DETAIL);
            SyncJobInfo latestPrReviewsJob = syncJobServicePort.getLatestJobForRepository(repoFullName, SyncJobType.PR_REVIEWS);
            SyncJobInfo latestPrCommitsJob = syncJobServicePort.getLatestJobForRepository(repoFullName, SyncJobType.PR_COMMITS);

            com.optimetric.github.domain.enumeration.SyncJobStatus overallStatus =
                    SyncJobMapper.determineOverallSyncStatus(latestPrListJob, latestPrDetailJob, latestPrReviewsJob, latestPrCommitsJob);
            OffsetDateTime lastSyncTime = SyncJobMapper.getLastSyncTime(latestPrListJob, latestPrDetailJob, latestPrReviewsJob, latestPrCommitsJob);
            return SyncJobMapper.toSyncStatusInfoFromJobInfos(repoFullName, overallStatus, jobs, lastSyncTime);
        } catch (Exception e) {
            log.error("Failed to get sync status for repository: {} by user: {}", repoFullName, login, e);
            throw new RuntimeException("Failed to retrieve synchronization status: " + e.getMessage(), e);
        }
    }

    private RepositoryEntity validateRepositoryAccess(String login, String repoFullName) 
            throws UserNotFoundException, RepositoryNotFoundException {
        
        // Kullanıcıyı Auth uygulama portu ile çöz (altyapı bağımlılığı yok)
        com.optimetric.auth.application.dto.UserInfo userInfo = authServicePort.findByLoginOrThrow(login);

        // fullName ve userId ile repoyu bul
        Optional<RepositoryEntity> repository = repositoryRepository.findByFullNameAndUserId(repoFullName, userInfo.id());
        
        if (repository.isEmpty()) {
            log.warn("Repository {} not found or not accessible by user: {}", repoFullName, login);
            throw new RepositoryNotFoundException("Repository not found or not accessible: " + repoFullName);
        }
        
        return repository.get();
    }

    public boolean needsSynchronization(PullRequestEntity pullRequest) {
        if (pullRequest == null) {
            return false;
        }
        return PullRequestSyncPolicy.needsSync(pullRequest.getLastSyncedAt(), pullRequest.getUpdatedAt());
    }

    @Transactional
    public void updateDataLevel(PullRequestEntity pullRequest) {
        if (pullRequest == null) {
            return;
        }
        boolean hasChangeStats = pullRequest.getAdditions() != null
                && pullRequest.getDeletions() != null
                && pullRequest.getChangedFiles() != null;
        DataLevel newDataLevel = DataLevelPolicy.resolveDataLevel(hasChangeStats);
        if (pullRequest.getDataLevel() != newDataLevel) {
            log.debug("Updating data level for PR {} from {} to {}",
                    pullRequest.getGithubPrId(), pullRequest.getDataLevel(), newDataLevel);
            pullRequest.setDataLevel(newDataLevel);
            pullRequestRepository.save(pullRequest);
        }
    }

    @Transactional
    public void updateLastSyncedAt(PullRequestEntity pullRequest) {
        if (pullRequest != null) {
            pullRequest.setLastSyncedAt(OffsetDateTime.now());
            pullRequestRepository.save(pullRequest);
        }
    }

    private void validateInputParameters(String login, String repoFullName) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("User login cannot be null or empty");
        }
        
        if (repoFullName == null || repoFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository full name cannot be null or empty");
        }
        
        // Repo full name formatını doğrula (owner/repo)
        if (!repoFullName.matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$")) {
            throw new InvalidRepositoryFormatException(
                "Invalid repository format: '" + repoFullName + "'. Expected format: 'owner/repo'");
        }
    }

    private void validatePrNumber(Integer prNumber) {
        if (prNumber == null) {
            throw new IllegalArgumentException("Pull request number cannot be null");
        }
        
        if (prNumber <= 0) {
            throw new IllegalArgumentException("Pull request number must be positive, got: " + prNumber);
        }
    }
}
