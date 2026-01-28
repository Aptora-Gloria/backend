package com.optimetric.github.infrastructure.persistence.adapter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimetric.github.api.response.RepositoryAnalyzeResponse;
import com.optimetric.github.application.port.BranchServicePort;
import com.optimetric.github.application.port.CommitServicePort;
import com.optimetric.github.application.port.LanguageServicePort;
import com.optimetric.github.application.port.RepoSyncStatusServicePort;
import com.optimetric.github.application.port.RepositoryAnalyzeServicePort;
import com.optimetric.github.application.port.SyncJobServicePort;
import com.optimetric.github.domain.enumeration.AnalyzeState;
import com.optimetric.github.domain.enumeration.EnqueuedJobType;
import com.optimetric.github.domain.enumeration.MissingComponent;
import com.optimetric.github.domain.enumeration.ReadinessState;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobStatus;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobType;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.github.infrastructure.persistence.jpa.SyncJobRepository;
import com.optimetric.shared.exception.BranchNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapter for repository analyze service.
 * Analyzes repository data readiness and enqueues sync jobs for missing components.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryAnalyzeServiceAdapter implements RepositoryAnalyzeServicePort {

    private final RepoSyncStatusServicePort repoSyncStatusService;
    private final SyncJobServicePort syncJobServicePort;
    private final SyncJobRepository syncJobRepository;
    private final BranchServicePort branchServicePort;
    private final CommitServicePort commitServicePort;
    private final LanguageServicePort languageServicePort;
    private final RepositoryRepository repositoryRepository;

    // Default commit sync period: 90 days
    private static final int DEFAULT_COMMIT_SYNC_DAYS = 90;

    @Override
    @Transactional
    public RepositoryAnalyzeResponse analyze(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        log.info("Analyzing repository: {} for user: {}", fullName, login);

        // Get readiness status (this may throw RepositoryNotFoundException if repo doesn't exist)
        var readiness = repoSyncStatusService.getReadiness(fullName);

        // If already ready, return immediately
        if (readiness.state() == ReadinessState.READY) {
            log.info("Repository {} is already ready, no jobs needed", fullName);
            return new RepositoryAnalyzeResponse(
                    AnalyzeState.ALREADY_READY,
                    List.of(),
                    "/api/github/repositories/sync/status"
            );
        }

        // Check if sync is already running
        List<SyncJobStatus> activeStatuses = List.of(SyncJobStatus.PENDING, SyncJobStatus.RUNNING);
        boolean hasActiveJobs = syncJobRepository.findByRepoFullNameOrderBySystemCreatedAtDesc(fullName)
                .stream()
                .anyMatch(job -> activeStatuses.contains(job.getStatus()));

        if (hasActiveJobs) {
            log.info("Repository {} has active sync jobs, skipping enqueue", fullName);
            return new RepositoryAnalyzeResponse(
                    AnalyzeState.SYNC_ALREADY_RUNNING,
                    List.of(),
                    "/api/github/repositories/sync/status"
            );
        }

        // Enqueue jobs based on missing components
        List<EnqueuedJobType> enqueued = new ArrayList<>();

        // Map missing components to job types and enqueue
        for (MissingComponent missing : readiness.missing()) {
            Optional<SyncJobType> jobType = mapMissingComponentToJobType(missing, readiness);
            
            if (jobType.isPresent()) {
                // Special case: PR_LIST execution already creates its own SyncJobEntity
                // so we should not create a duplicate job row here.
                if (jobType.get() == SyncJobType.PR_LIST) {
                    try {
                        enqueued.add(EnqueuedJobType.PR_LIST);
                        executeJob(login, fullName, SyncJobType.PR_LIST);
                    } catch (Exception e) {
                        log.error("Failed to execute PR_LIST for repository: {}", fullName, e);
                    }
                    continue;
                }

                // Check idempotency: don't create if PENDING or RUNNING job exists
                Optional<com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity> existingJob = 
                        syncJobRepository.findByRepoFullNameAndJobTypeAndStatus(
                                fullName, jobType.get(), SyncJobStatus.PENDING);
                
                if (existingJob.isEmpty()) {
                    existingJob = syncJobRepository.findByRepoFullNameAndJobTypeAndStatus(
                            fullName, jobType.get(), SyncJobStatus.RUNNING);
                }

                if (existingJob.isEmpty()) {
                    try {
                        // Create job
                        var createdJob = syncJobServicePort.createJob(jobType.get(), fullName);
                        enqueued.add(mapJobTypeToEnqueuedType(jobType.get()));
                        log.info("Created {} job (id={}) for repository: {}", jobType.get(), createdJob.getId(), fullName);
                        
                        // Execute job immediately (MVP: synchronous execution)
                        executeJob(login, fullName, jobType.get());

                        // Mark job completed so it doesn't block future analyze calls
                        syncJobServicePort.updateJobStatus(createdJob.getId(), SyncJobStatus.COMPLETED, null);
                    } catch (Exception e) {
                        log.error("Failed to create/execute {} job for repository: {}", jobType.get(), fullName, e);
                        // Continue with other jobs even if one fails
                    }
                } else {
                    log.debug("Job {} already exists (PENDING/RUNNING) for repository: {}, skipping", 
                            jobType.get(), fullName);
                }
            }
        }

        log.info("Analysis complete for repository: {}, enqueued {} jobs", fullName, enqueued.size());

        // If no jobs were enqueued (all were skipped due to idempotency or dependencies),
        // but we're not ready, it means jobs are already running
        if (enqueued.isEmpty() && readiness.state() != ReadinessState.READY) {
            log.info("No jobs enqueued for repository: {} - likely already running", fullName);
            return new RepositoryAnalyzeResponse(
                    AnalyzeState.SYNC_ALREADY_RUNNING,
                    List.of(),
                    "/api/github/repositories/sync/status"
            );
        }

        return new RepositoryAnalyzeResponse(
                AnalyzeState.SYNC_STARTED,
                enqueued,
                "/api/github/repositories/sync/status"
        );
    }

    /**
     * Maps a missing component to the appropriate job type.
     * Handles special cases like branch dependency for commits.
     */
    private Optional<SyncJobType> mapMissingComponentToJobType(
            MissingComponent missing, 
            com.optimetric.github.api.response.RepositoryReadinessDto readiness) {
        
        return switch (missing) {
            case BRANCHES -> Optional.of(SyncJobType.BRANCH_SYNC);
            case PRS -> Optional.of(SyncJobType.PR_LIST);
            case LANGUAGES -> Optional.of(SyncJobType.LANGUAGE_SYNC);
            case COMMITS -> {
                // Don't enqueue COMMIT_SYNC if branches are not ready
                // Branch sync should be enqueued first
                if (!readiness.flags().branchesReady()) {
                    log.debug("Skipping COMMIT_SYNC for {} - branches not ready, BRANCH_SYNC should be enqueued first", 
                            missing);
                    yield Optional.empty();
                }
                yield Optional.of(SyncJobType.COMMIT_SYNC);
            }
            case PR_DETAILS, REVIEWS -> {
                // PR_DETAILS and REVIEWS are handled by PR job chain
                // PR_LIST will trigger PR_DETAIL, which triggers PR_REVIEWS
                // Only enqueue if PR_LIST is not already enqueued
                if (!readiness.missing().contains(MissingComponent.PRS)) {
                    // PRS exist, but details/reviews are missing
                    // This means PR_LIST job completed but PR_DETAIL chain didn't run
                    // For MVP, we don't enqueue PR_DETAIL/REVIEWS separately
                    // They should be handled by the job chain after PR_LIST completes
                    log.debug("PR_DETAILS/REVIEWS missing but PRS exist - will be handled by PR job chain");
                }
                yield Optional.empty();
            }
        };
    }

    /**
     * Maps SyncJobType to EnqueuedJobType for response DTO.
     * Only maps job types that can be enqueued by analyze endpoint.
     */
    private EnqueuedJobType mapJobTypeToEnqueuedType(SyncJobType jobType) {
        return switch (jobType) {
            case BRANCH_SYNC -> EnqueuedJobType.BRANCH_SYNC;
            case PR_LIST -> EnqueuedJobType.PR_LIST;
            case LANGUAGE_SYNC -> EnqueuedJobType.LANGUAGE_SYNC;
            case COMMIT_SYNC -> EnqueuedJobType.COMMIT_SYNC;
            case PR_DETAIL, PR_REVIEWS, PR_COMMITS -> {
                // These are not directly enqueued by analyze endpoint
                // They are part of the PR job chain
                log.warn("Unexpected job type in mapJobTypeToEnqueuedType: {} - this should not happen", jobType);
                throw new IllegalArgumentException("Job type " + jobType + " cannot be directly enqueued by analyze endpoint");
            }
        };
    }

    /**
     * Executes a sync job immediately (MVP: synchronous execution).
     * In a production system, this would be handled by a job scheduler.
     */
    private void executeJob(String login, String fullName, SyncJobType jobType) {
        try {
            switch (jobType) {
                case BRANCH_SYNC -> {
                    log.info("Executing BRANCH_SYNC for repository: {}", fullName);
                    branchServicePort.initRepoBranches(login, fullName);
                }
                case LANGUAGE_SYNC -> {
                    log.info("Executing LANGUAGE_SYNC for repository: {}", fullName);
                    languageServicePort.initRepoLanguages(login, fullName);
                }
                case COMMIT_SYNC -> {
                    log.info("Executing COMMIT_SYNC for repository: {}", fullName);
                    // Get repository to find default branch
                    RepositoryEntity repository = repositoryRepository.findByFullName(fullName)
                            .orElseThrow(() -> new RepositoryNotFoundException("Repository not found: " + fullName));
                    
                    String branchName = repository.getDefaultBranch();
                    if (branchName == null || branchName.isBlank()) {
                        branchName = "main"; // Fallback to "main" if default branch is not set
                        log.warn("Default branch not set for repository: {}, using 'main' as fallback", fullName);
                    }
                    
                    // Calculate since date (90 days ago)
                    OffsetDateTime since = OffsetDateTime.now().minusDays(DEFAULT_COMMIT_SYNC_DAYS);
                    String sinceStr = since.toInstant().toString(); // ISO-8601 format
                    
                    commitServicePort.initCommits(login, fullName, branchName, sinceStr);
                }
                case PR_LIST -> {
                    log.info("Executing PR_LIST for repository: {}", fullName);
                    syncJobServicePort.executePrListJob(fullName);
                }
                default -> {
                    log.warn("Job type {} is not directly executable by analyze endpoint", jobType);
                }
            }
            log.info("Successfully executed {} job for repository: {}", jobType, fullName);
        } catch (BranchNotFoundException e) {
            log.error("Branch not found for COMMIT_SYNC job: {}", fullName, e);
            throw e;
        } catch (RepositoryNotFoundException e) {
            log.error("Repository not found for {} job: {}", jobType, fullName, e);
            throw e;
        } catch (UserNotFoundException e) {
            log.error("User not found for {} job: {}", jobType, fullName, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to execute {} job for repository: {}", jobType, fullName, e);
            throw new RuntimeException("Failed to execute " + jobType + " job for repository: " + fullName, e);
        }
    }
}
