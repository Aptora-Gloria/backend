package com.optimetric.github.application.port;

import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobStatus;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobType;

import java.util.List;

/**
 * Service port for Sync Job operations.
 * Provides business logic for managing synchronization jobs.
 */
public interface SyncJobServicePort {

    /**
     * Executes a PR_LIST job to discover and upsert basic PR records.
     * 
     * @param repoFullName Repository full name (e.g., "owner/repo")
     */
    void executePrListJob(String repoFullName);

    /**
     * Executes a PR_DETAIL job to fetch change statistics for a specific PR.
     * 
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @param pullRequestId Internal Pull Request ID
     */
    void executePrDetailJob(String repoFullName, Long pullRequestId);

    /**
     * Executes a PR_REVIEWS job to fetch reviews for a specific PR.
     * 
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @param pullRequestId Internal Pull Request ID
     */
    void executePrReviewsJob(String repoFullName, Long pullRequestId);

    /**
     * Executes a PR_COMMITS job to fetch commits for a specific PR.
     * 
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @param pullRequestId Internal Pull Request ID
     */
    void executePrCommitsJob(String repoFullName, Long pullRequestId);

    /**
     * Creates a new sync job with the specified type and repository.
     * 
     * @param jobType Type of sync job to create
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return Created sync job entity
     */
    SyncJobEntity createJob(SyncJobType jobType, String repoFullName);

    /**
     * Updates the status of an existing sync job.
     * 
     * @param jobId Sync job ID
     * @param status New status for the job
     * @param errorMessage Error message if job failed (can be null)
     */
    void updateJobStatus(Long jobId, SyncJobStatus status, String errorMessage);

    /**
     * Retrieves all pending sync jobs.
     * 
     * @return List of pending sync job entities
     */
    List<SyncJobEntity> getPendingJobs();

    /**
     * Retrieves sync jobs for a specific repository.
     * 
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return List of sync job entities for the repository
     */
    List<SyncJobEntity> getJobsForRepository(String repoFullName);

    /**
     * Retrieves the latest sync job of a specific type for a repository.
     * 
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @param jobType Type of sync job
     * @return Latest sync job entity or null if none found
     */
    SyncJobEntity getLatestJobForRepository(String repoFullName, SyncJobType jobType);
}