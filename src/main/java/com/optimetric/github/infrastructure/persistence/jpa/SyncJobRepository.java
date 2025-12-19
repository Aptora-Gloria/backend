package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobStatus;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SyncJobRepository extends JpaRepository<SyncJobEntity, Long> {

    // Find jobs by status
    List<SyncJobEntity> findByStatusOrderBySystemCreatedAtAsc(SyncJobStatus status);

    // Find jobs by job type and status
    List<SyncJobEntity> findByJobTypeAndStatusOrderBySystemCreatedAtAsc(SyncJobType jobType, SyncJobStatus status);

    // Find jobs by repository full name
    List<SyncJobEntity> findByRepoFullNameOrderBySystemCreatedAtDesc(String repoFullName);

    // Find jobs by repository and job type
    List<SyncJobEntity> findByRepoFullNameAndJobTypeOrderBySystemCreatedAtDesc(String repoFullName, SyncJobType jobType);

    // Find jobs by repository, job type, and status
    Optional<SyncJobEntity> findByRepoFullNameAndJobTypeAndStatus(String repoFullName, SyncJobType jobType, SyncJobStatus status);

    // Find pending jobs that are ready to run
    @Query("SELECT sj FROM SyncJobEntity sj WHERE sj.status = :status " +
           "AND (sj.nextRunAt IS NULL OR sj.nextRunAt <= :now) " +
           "ORDER BY sj.systemCreatedAt ASC")
    List<SyncJobEntity> findPendingJobsReadyToRun(@Param("status") SyncJobStatus status, @Param("now") OffsetDateTime now);

    // Find failed jobs that can be retried
    @Query("SELECT sj FROM SyncJobEntity sj WHERE sj.status = :status " +
           "AND sj.attemptCount < :maxAttempts " +
           "AND (sj.nextRunAt IS NULL OR sj.nextRunAt <= :now) " +
           "ORDER BY sj.systemCreatedAt ASC")
    List<SyncJobEntity> findFailedJobsForRetry(@Param("status") SyncJobStatus status, 
                                               @Param("maxAttempts") Integer maxAttempts, 
                                               @Param("now") OffsetDateTime now);

    // Find running jobs that might be stuck (started more than X minutes ago)
    @Query("SELECT sj FROM SyncJobEntity sj WHERE sj.status = :status " +
           "AND sj.startedAt < :stuckThreshold")
    List<SyncJobEntity> findStuckJobs(@Param("status") SyncJobStatus status, @Param("stuckThreshold") OffsetDateTime stuckThreshold);

    // Count jobs by status
    long countByStatus(SyncJobStatus status);

    // Count jobs by repository and status
    long countByRepoFullNameAndStatus(String repoFullName, SyncJobStatus status);

    // Find latest completed job for a repository and job type
    Optional<SyncJobEntity> findFirstByRepoFullNameAndJobTypeAndStatusOrderByCompletedAtDesc(
            String repoFullName, SyncJobType jobType, SyncJobStatus status);

    // Find jobs that need cleanup (completed/failed jobs older than X days)
    @Query("SELECT sj FROM SyncJobEntity sj WHERE sj.status IN (:statuses) " +
           "AND sj.systemUpdatedAt < :cleanupThreshold")
    List<SyncJobEntity> findJobsForCleanup(@Param("statuses") List<SyncJobStatus> statuses, 
                                           @Param("cleanupThreshold") OffsetDateTime cleanupThreshold);
}