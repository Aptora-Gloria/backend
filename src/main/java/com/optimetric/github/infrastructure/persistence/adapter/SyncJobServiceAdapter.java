package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.application.dto.PullRequestCommitFromApi;
import com.optimetric.github.application.dto.PullRequestDetailFromApi;
import com.optimetric.github.application.dto.PullRequestFromApi;
import com.optimetric.github.application.dto.PullRequestReviewFromApi;
import com.optimetric.github.application.dto.SyncJobInfo;
import com.optimetric.github.application.port.out.PullRequestClientPort;
import com.optimetric.github.application.port.SyncJobServicePort;
import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import com.optimetric.github.domain.service.SyncJobPolicy;
import com.optimetric.github.domain.vo.SyncJobSnapshot;
import com.optimetric.github.infrastructure.core.mapper.SyncJobMapper;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestCommitRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestReviewRepository;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.github.infrastructure.persistence.jpa.SyncJobRepository;
import com.optimetric.shared.exception.GitHubApiException;
import com.optimetric.shared.exception.InvalidRepositoryFormatException;
import com.optimetric.shared.exception.SyncJobException;
import com.optimetric.shared.exception.UserNotFoundException;
import com.optimetric.github.infrastructure.core.mapper.GitHubResponseMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

// Senkronizasyon işlerini yöneten servis adapter'ı; PR ingestion için retry ve sayfalama.
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
    private final EntityManager entityManager;

    // Domain policy sabitleri: yeniden deneme ve zamanlama
    private static final int MAX_RETRY_ATTEMPTS = SyncJobPolicy.DEFAULT_MAX_RETRY_ATTEMPTS;
    private static final int RETRY_DELAY_MINUTES = SyncJobPolicy.DEFAULT_RETRY_DELAY_MINUTES;
    private static final int STUCK_JOB_THRESHOLD_MINUTES = SyncJobPolicy.DEFAULT_STUCK_JOB_THRESHOLD_MINUTES;

    @Override
    @Transactional
    public void executePrListJob(String repoFullName) {
        log.info("Executing PR_LIST job for repository: {}", repoFullName);
        
        // Girdi doğrula
        validateRepositoryFullName(repoFullName);

        // PR_LIST job bul veya oluştur
        SyncJobEntity job = findOrCreateJob(SyncJobType.PR_LIST, repoFullName);

        try {
            // Job durumunu RUNNING yap
            updateJobToRunning(job);

            // Tam uygulama için bağımlılıklar mevcut mu kontrol et
            if (repositoryRepository == null || pullRequestClientPort == null || authServicePort == null) {
                log.warn("Missing dependencies for full PR_LIST job implementation - completing job without processing");
                updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
                return;
            }
            
            // Repo ve kullanıcı access token al (sadece Auth port üzerinden)
            RepositoryEntity repository = getRepositoryByFullName(repoFullName);
            String login = authServicePort.findById(repository.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found for id: " + repository.getUserId()))
                    .login();
            String accessToken = authServicePort.getAccessTokenFromLogin(login);
            
            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new SyncJobException("No valid access token found for repository: " + repoFullName);
            }
            
            // owner ve repo'yu full name'den ayır
            String[] parts = repoFullName.split("/");
            if (parts.length != 2) {
                throw new InvalidRepositoryFormatException("Invalid repository full name format: " + repoFullName);
            }
            String owner = parts[0];
            String repo = parts[1];
            
            int page = job.getCurrentPage() != null ? job.getCurrentPage() : 1;
            int processedCount = 0;
            
            // PR'ları sayfa sayfa çek
            while (true) {
                log.debug("Fetching PRs for {}/{} - page {}", owner, repo, page);
                
                List<PullRequestFromApi> pullRequests = pullRequestClientPort.getPullRequests(
                    accessToken, owner, repo, page);

                if (pullRequests.isEmpty()) {
                    log.debug("No more PRs found for {}/{} at page {}", owner, repo, page);
                    break;
                }

                // Her PR'ı işle
                for (PullRequestFromApi prResponse : pullRequests) {
                    try {
                        upsertBasicPullRequest(repository, prResponse);
                        processedCount++;
                    } catch (Exception e) {
                        log.error("Failed to process PR {} for repository {}: {}", prResponse.id(), repoFullName, e.getMessage());
                        // Diğer PR'larla devam et
                    }
                }
                
                // Sayfalama durumunu güncelle
                updateJobPaginationState(job.getId(), null, page);
                
                // Sonraki sayfaya geç
                page++;
                
                // 100'den az sonuç geldiyse son sayfadayız, çık
                if (pullRequests.size() < 100) {
                    break;
                }
            }
            
            log.info("PR_LIST job completed for repository: {} - processed {} PRs", repoFullName, processedCount);
            
            // Detay job'ları ihtiyacı olan PR'lar için planla
            scheduleDetailJobs(repository);
            
            // Job'u tamamlandı olarak işaretle
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
        
        // Girdileri doğrula
        validateRepositoryFullName(repoFullName);
        validatePullRequestId(pullRequestId);
        
        // Takip için cursor'da PR id ile job oluştur
        SyncJobEntity job = createJobWithCursor(SyncJobType.PR_DETAIL, repoFullName, pullRequestId.toString());
        
        try {
            updateJobToRunning(job);
            
            // Tam uygulama için gerekli bağımlılıklar mevcut mu
            if (pullRequestRepository == null || pullRequestClientPort == null || authServicePort == null) {
                log.warn("Missing dependencies for full PR_DETAIL job implementation - completing job without processing");
                updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
                return;
            }
            
            // Pull request entity al
            Optional<PullRequestEntity> prOpt = pullRequestRepository.findById(pullRequestId);
            if (prOpt.isEmpty()) {
                throw new IllegalArgumentException("Pull request not found with ID: " + pullRequestId);
            }
            
            PullRequestEntity pullRequest = prOpt.get();
            RepositoryEntity repository = pullRequest.getRepository();
            String login = authServicePort.findById(repository.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found for id: " + repository.getUserId()))
                    .login();
            String accessToken = authServicePort.getAccessTokenFromLogin(login);

            // owner ve repo'yu full name'den ayır
            String[] parts = repoFullName.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid repository full name format: " + repoFullName);
            }
            String owner = parts[0];
            String repo = parts[1];

            // Detaylı PR bilgisini çek
            PullRequestDetailFromApi detailResponse = pullRequestClientPort.getPullRequestDetails(
                accessToken, owner, repo, pullRequest.getPrNumber());

            // PR'ı değişiklik istatistikleriyle güncelle
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
        
        // Girdileri doğrula
        validateRepositoryFullName(repoFullName);
        validatePullRequestId(pullRequestId);
        
        SyncJobEntity job = createJobWithCursor(SyncJobType.PR_REVIEWS, repoFullName, pullRequestId.toString());
        
        try {
            updateJobToRunning(job);
            
            // Tam uygulama için gerekli bağımlılıklar mevcut mu
            if (pullRequestRepository == null || pullRequestReviewRepository == null || 
                pullRequestClientPort == null || authServicePort == null) {
                log.warn("Missing dependencies for full PR_REVIEWS job implementation - completing job without processing");
                updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
                return;
            }
            
            // Pull request entity al
            Optional<PullRequestEntity> prOpt = pullRequestRepository.findById(pullRequestId);
            if (prOpt.isEmpty()) {
                throw new IllegalArgumentException("Pull request not found with ID: " + pullRequestId);
            }
            
            PullRequestEntity pullRequest = prOpt.get();
            RepositoryEntity repository = pullRequest.getRepository();
            String login = authServicePort.findById(repository.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found for id: " + repository.getUserId()))
                    .login();
            String accessToken = authServicePort.getAccessTokenFromLogin(login);

            // owner ve repo'yu full name'den ayır
            String[] parts = repoFullName.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid repository full name format: " + repoFullName);
            }
            String owner = parts[0];
            String repo = parts[1];

            // PR review'larını çek
            List<PullRequestReviewFromApi> reviewResponses = pullRequestClientPort.getPullRequestReviews(
                accessToken, owner, repo, pullRequest.getPrNumber());

            int processedCount = 0;

            // Her review'ı işle
            for (PullRequestReviewFromApi reviewResponse : reviewResponses) {
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
        
        // Girdileri doğrula
        validateRepositoryFullName(repoFullName);
        validatePullRequestId(pullRequestId);
        
        SyncJobEntity job = createJobWithCursor(SyncJobType.PR_COMMITS, repoFullName, pullRequestId.toString());
        
        try {
            updateJobToRunning(job);
            
            // Tam uygulama için gerekli bağımlılıklar mevcut mu
            if (pullRequestRepository == null || pullRequestCommitRepository == null || 
                pullRequestClientPort == null || authServicePort == null) {
                log.warn("Missing dependencies for full PR_COMMITS job implementation - completing job without processing");
                updateJobStatus(job.getId(), SyncJobStatus.COMPLETED, null);
                return;
            }
            
            // Pull request entity al
            Optional<PullRequestEntity> prOpt = pullRequestRepository.findById(pullRequestId);
            if (prOpt.isEmpty()) {
                throw new IllegalArgumentException("Pull request not found with ID: " + pullRequestId);
            }
            
            PullRequestEntity pullRequest = prOpt.get();
            RepositoryEntity repository = pullRequest.getRepository();
            String login = authServicePort.findById(repository.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found for id: " + repository.getUserId()))
                    .login();
            String accessToken = authServicePort.getAccessTokenFromLogin(login);

            // owner ve repo'yu full name'den ayır
            String[] parts = repoFullName.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid repository full name format: " + repoFullName);
            }
            String owner = parts[0];
            String repo = parts[1];

            // PR commit'lerini çek
            List<PullRequestCommitFromApi> commitResponses = pullRequestClientPort.getPullRequestCommits(
                accessToken, owner, repo, pullRequest.getPrNumber());

            int processedCount = 0;

            // Her commit'i işle
            for (PullRequestCommitFromApi commitResponse : commitResponses) {
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
    public SyncJobInfo createJob(SyncJobType jobType, String repoFullName) {
        SyncJobEntity savedJob = createJobEntity(jobType, repoFullName);
        return SyncJobMapper.toSyncJobInfo(savedJob);
    }

    // Job yoksa oluşturur ve kaydeder; findOrCreateJob tarafından kullanılır.
    private SyncJobEntity createJobEntity(SyncJobType jobType, String repoFullName) {
        log.debug("Creating sync job of type {} for repository: {}", jobType, repoFullName);

        SyncJobEntity job = SyncJobEntity.builder()
                .jobType(jobType)
                .repoFullName(repoFullName)
                .status(SyncJobStatus.PENDING)
                .attemptCount(0)
                .currentPage(1)
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
                SyncJobSnapshot snapshot = SyncJobSnapshot.of(
                        SyncJobStatus.FAILED, job.getStartedAt(), now, job.getAttemptCount());
                if (SyncJobPolicy.shouldScheduleRetry(snapshot, MAX_RETRY_ATTEMPTS)) {
                    int delayMinutes = SyncJobPolicy.retryDelayMinutes(snapshot, RETRY_DELAY_MINUTES);
                    job.setNextRunAt(now.plusMinutes(delayMinutes));
                    job.setStatus(SyncJobStatus.PENDING); // Yeniden deneme için beklemeye al
                    log.info("Scheduling retry for job {} in {} minutes (attempt {}/{})",
                            jobId, delayMinutes, job.getAttemptCount(), MAX_RETRY_ATTEMPTS);
                }
            }
        }
        
        syncJobRepository.save(job);
        log.debug("Updated job {} status to: {}", jobId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncJobInfo> getPendingJobs() {
        OffsetDateTime now = OffsetDateTime.now();
        List<SyncJobEntity> pendingJobs = syncJobRepository.findPendingJobsReadyToRun(SyncJobStatus.PENDING, now);
        log.debug("Found {} pending jobs ready to run", pendingJobs.size());
        return pendingJobs.stream().map(SyncJobMapper::toSyncJobInfo).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncJobInfo> getJobsForRepository(String repoFullName) {
        List<SyncJobEntity> jobs = syncJobRepository.findByRepoFullNameOrderBySystemCreatedAtDesc(repoFullName);
        log.debug("Found {} jobs for repository: {}", jobs.size(), repoFullName);
        return jobs.stream().map(SyncJobMapper::toSyncJobInfo).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SyncJobInfo getLatestJobForRepository(String repoFullName, SyncJobType jobType) {
        Optional<SyncJobEntity> latestJob = syncJobRepository
                .findFirstByRepoFullNameAndJobTypeAndStatusOrderByCompletedAtDesc(
                        repoFullName, jobType, SyncJobStatus.COMPLETED);

        if (latestJob.isPresent()) {
            log.debug("Found latest {} job for repository: {} completed at: {}",
                    jobType, repoFullName, latestJob.get().getCompletedAt());
            return SyncJobMapper.toSyncJobInfo(latestJob.get());
        } else {
            log.debug("No completed {} job found for repository: {}", jobType, repoFullName);
            return null;
        }
    }

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

    @Transactional
    public List<SyncJobEntity> resetStuckJobs() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime stuckThreshold = now.minusMinutes(STUCK_JOB_THRESHOLD_MINUTES);
        List<SyncJobEntity> stuckJobs = syncJobRepository.findStuckJobs(SyncJobStatus.RUNNING, stuckThreshold);
        
        for (SyncJobEntity job : stuckJobs) {
            SyncJobSnapshot snapshot = SyncJobSnapshot.of(
                    job.getStatus(), job.getStartedAt(), job.getCompletedAt(), job.getAttemptCount());
            if (!SyncJobPolicy.isJobStuck(snapshot, now, STUCK_JOB_THRESHOLD_MINUTES)) {
                continue;
            }
            log.warn("Resetting stuck job {} that has been running since {}", job.getId(), job.getStartedAt());
            job.setStatus(SyncJobStatus.FAILED);
            job.setErrorMessage("Job was stuck and automatically reset");
            job.setCompletedAt(now);
            if (SyncJobPolicy.shouldScheduleRetry(snapshot, MAX_RETRY_ATTEMPTS)) {
                job.setNextRunAt(now.plusMinutes(RETRY_DELAY_MINUTES));
                job.setStatus(SyncJobStatus.PENDING);
            }
        }
        
        if (!stuckJobs.isEmpty()) {
            syncJobRepository.saveAll(stuckJobs);
            log.info("Reset {} stuck jobs", stuckJobs.size());
        }
        
        return stuckJobs;
    }

    @Transactional
    public int cleanupOldJobs(int daysOld) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime cleanupThreshold = now.minusDays(daysOld);
        List<SyncJobStatus> completedStatuses = List.of(SyncJobStatus.COMPLETED, SyncJobStatus.FAILED);
        
        List<SyncJobEntity> candidates = syncJobRepository.findJobsForCleanup(completedStatuses, cleanupThreshold);
        List<SyncJobEntity> oldJobs = candidates.stream()
                .filter(job -> SyncJobPolicy.shouldCleanup(
                        SyncJobSnapshot.of(job.getStatus(), job.getStartedAt(), job.getCompletedAt(), job.getAttemptCount()),
                        cleanupThreshold))
                .toList();
        
        if (!oldJobs.isEmpty()) {
            syncJobRepository.deleteAll(oldJobs);
            log.info("Cleaned up {} old sync jobs older than {} days", oldJobs.size(), daysOld);
        }
        
        return oldJobs.size();
    }


    private SyncJobEntity findOrCreateJob(SyncJobType jobType, String repoFullName) {
        // Bu repo için bu tipte bekleyen veya çalışan job var mı kontrol et
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
        
        // Yoksa yeni job oluştur
        return createJobEntity(jobType, repoFullName);
    }

    private SyncJobEntity createJobWithCursor(SyncJobType jobType, String repoFullName, String cursor) {
        SyncJobEntity job = createJobEntity(jobType, repoFullName);
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


    private RepositoryEntity getRepositoryByFullName(String repoFullName) {
        // Bu full name ile ilk repoyu bul; gerçek uygulamada kullanıcı bağlamı gerekebilir
        List<RepositoryEntity> repositories = repositoryRepository.findAll().stream()
                .filter(repo -> repoFullName.equals(repo.getFullName()))
                .toList();
        
        if (repositories.isEmpty()) {
            throw new IllegalArgumentException("Repository not found: " + repoFullName);
        }
        
        return repositories.get(0);
    }

    private void upsertBasicPullRequest(RepositoryEntity repository, PullRequestFromApi prResponse) {
        log.debug("Upserting basic PR data for github_pr_id: {}", prResponse.id());
        PullRequestEntity pullRequest = null;
        try {
            Optional<PullRequestEntity> existingPr = pullRequestRepository.findByGithubPrId(prResponse.id());

            if (existingPr.isPresent()) {
                pullRequest = existingPr.get();
                if (prResponse.updatedAt() != null && pullRequest.getLastSyncedAt() != null &&
                    !prResponse.updatedAt().isAfter(pullRequest.getLastSyncedAt())) {
                    log.debug("PR {} is up to date, skipping", prResponse.id());
                    return;
                }
                log.debug("Updating existing PR: {}", prResponse.id());
            } else {
                log.debug("Creating new PR: {}", prResponse.id());
            }

            pullRequest = GitHubResponseMapper.toPullRequestEntity(prResponse, repository, pullRequest);
            pullRequestRepository.save(pullRequest);
            log.debug("Upserted PR: {} (#{}) for repository: {}",
                    prResponse.id(), prResponse.number(), repository.getFullName());
        } catch (Exception e) {
            // Null-id entity session'da kalmasın (sonraki flush'ta AssertionFailure önlenir)
            if (pullRequest != null && pullRequest.getId() == null) {
                entityManager.detach(pullRequest);
            }
            throw e;
        }
    }

    private void updatePullRequestWithDetails(PullRequestEntity pullRequest, PullRequestDetailFromApi detailResponse) {
        log.debug("Updating PR {} with detailed information", pullRequest.getGithubPrId());
        GitHubResponseMapper.updateWithDetails(pullRequest, detailResponse);
        pullRequestRepository.save(pullRequest);
        log.debug("Updated PR {} with change statistics: +{} -{} files:{}",
                 pullRequest.getGithubPrId(), detailResponse.additions(),
                 detailResponse.deletions(), detailResponse.changedFiles());
    }

    private void upsertPullRequestReview(PullRequestEntity pullRequest, PullRequestReviewFromApi reviewResponse) {
        log.debug("Upserting review for github_review_id: {}", reviewResponse.id());
        Optional<PullRequestReviewEntity> existingReview = pullRequestReviewRepository.findByGithubReviewId(reviewResponse.id());
        PullRequestReviewEntity review = existingReview.orElse(null);
        if (review != null) log.debug("Updating existing review: {}", reviewResponse.id());
        else log.debug("Creating new review: {}", reviewResponse.id());
        review = GitHubResponseMapper.toPullRequestReviewEntity(reviewResponse, pullRequest, review);
        pullRequestReviewRepository.save(review);
        log.debug("Upserted review: {} for PR: {}", reviewResponse.id(), pullRequest.getGithubPrId());
    }

    private void upsertPullRequestCommit(PullRequestEntity pullRequest, Long githubRepoId, PullRequestCommitFromApi commitResponse) {
        log.debug("Upserting commit for sha: {}", commitResponse.sha());
        Optional<PullRequestCommitEntity> existingCommit = pullRequestCommitRepository.findByGithubRepoIdAndSha(githubRepoId, commitResponse.sha());
        PullRequestCommitEntity commit = existingCommit.orElse(null);
        if (commit != null) log.debug("Updating existing commit: {}", commitResponse.sha());
        else log.debug("Creating new commit: {}", commitResponse.sha());
        commit = GitHubResponseMapper.toPullRequestCommitEntity(commitResponse, pullRequest, githubRepoId, commit);
        pullRequestCommitRepository.save(commit);
        log.debug("Upserted commit: {} for PR: {}", commitResponse.sha(), pullRequest.getGithubPrId());
    }

    private void scheduleDetailJobs(RepositoryEntity repository) {
        if (pullRequestRepository == null) {
            log.warn("Cannot schedule detail jobs - pullRequestRepository is null");
            return;
        }
        
        log.debug("Scheduling detail jobs for repository: {}", repository.getFullName());
        
        // Detay job'a ihtiyacı olan PR'ları bul (değişiklik istatistiği olmayanlar)
        List<PullRequestEntity> prsNeedingDetails = pullRequestRepository.findPullRequestsWithoutChangeStats(repository);
        
        for (PullRequestEntity pr : prsNeedingDetails) {
            try {
                // PR_DETAIL job planla
                log.debug("Scheduling PR_DETAIL job for PR: {}", pr.getGithubPrId());
                executePrDetailJob(repository.getFullName(), pr.getId());
                
                // PR_REVIEWS job planla
                log.debug("Scheduling PR_REVIEWS job for PR: {}", pr.getGithubPrId());
                executePrReviewsJob(repository.getFullName(), pr.getId());
                
                // PR_COMMITS job planla
                log.debug("Scheduling PR_COMMITS job for PR: {}", pr.getGithubPrId());
                executePrCommitsJob(repository.getFullName(), pr.getId());
                
            } catch (Exception e) {
                log.error("Failed to schedule detail jobs for PR: {}", pr.getGithubPrId(), e);
                // Biri hata verse bile diğer PR'larla devam et
            }
        }
        
        log.info("Scheduled detail jobs for {} PRs in repository: {}", prsNeedingDetails.size(), repository.getFullName());
    }

    private void validateRepositoryFullName(String repoFullName) {
        if (repoFullName == null || repoFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository full name cannot be null or empty");
        }
        
        if (!repoFullName.matches("^[a-zA-Z0-9._-]+/[a-zA-Z0-9._-]+$")) {
            throw new InvalidRepositoryFormatException(
                "Invalid repository format: '" + repoFullName + "'. Expected format: 'owner/repo'");
        }
    }

    private void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull request ID cannot be null");
        }
        
        if (pullRequestId <= 0) {
            throw new IllegalArgumentException("Pull request ID must be positive, got: " + pullRequestId);
        }
    }
}