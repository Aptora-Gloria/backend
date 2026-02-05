package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SyncJobRepository extends JpaRepository<SyncJobEntity, Long> {

    // Duruma göre job'ları getir
    List<SyncJobEntity> findByStatusOrderBySystemCreatedAtAsc(SyncJobStatus status);

    // Job tipi ve duruma göre getir
    List<SyncJobEntity> findByJobTypeAndStatusOrderBySystemCreatedAtAsc(SyncJobType jobType, SyncJobStatus status);

    // Repo full name ile job'ları getir
    List<SyncJobEntity> findByRepoFullNameOrderBySystemCreatedAtDesc(String repoFullName);

    // Repo ve job tipi ile getir
    List<SyncJobEntity> findByRepoFullNameAndJobTypeOrderBySystemCreatedAtDesc(String repoFullName, SyncJobType jobType);

    // Repo, job tipi ve durum ile tek job bul
    Optional<SyncJobEntity> findByRepoFullNameAndJobTypeAndStatus(String repoFullName, SyncJobType jobType, SyncJobStatus status);

    // Çalışmaya hazır bekleyen job'lar
    @Query("SELECT sj FROM SyncJobEntity sj WHERE sj.status = :status " +
           "AND (sj.nextRunAt IS NULL OR sj.nextRunAt <= :now) " +
           "ORDER BY sj.systemCreatedAt ASC")
    List<SyncJobEntity> findPendingJobsReadyToRun(@Param("status") SyncJobStatus status, @Param("now") OffsetDateTime now);

    // Yeniden denemeye uygun başarısız job'lar
    @Query("SELECT sj FROM SyncJobEntity sj WHERE sj.status = :status " +
           "AND sj.attemptCount < :maxAttempts " +
           "AND (sj.nextRunAt IS NULL OR sj.nextRunAt <= :now) " +
           "ORDER BY sj.systemCreatedAt ASC")
    List<SyncJobEntity> findFailedJobsForRetry(@Param("status") SyncJobStatus status, 
                                               @Param("maxAttempts") Integer maxAttempts, 
                                               @Param("now") OffsetDateTime now);

    // Takılmış olabilecek çalışan job'lar (X dakikadan önce başlayan)
    @Query("SELECT sj FROM SyncJobEntity sj WHERE sj.status = :status " +
           "AND sj.startedAt < :stuckThreshold")
    List<SyncJobEntity> findStuckJobs(@Param("status") SyncJobStatus status, @Param("stuckThreshold") OffsetDateTime stuckThreshold);

    // Duruma göre job sayısı
    long countByStatus(SyncJobStatus status);

    // Repo ve duruma göre job sayısı
    long countByRepoFullNameAndStatus(String repoFullName, SyncJobStatus status);

    // Repo ve job tipi için en son tamamlanan job
    Optional<SyncJobEntity> findFirstByRepoFullNameAndJobTypeAndStatusOrderByCompletedAtDesc(
            String repoFullName, SyncJobType jobType, SyncJobStatus status);

    // Temizlik gerektiren job'lar (X günden eski tamamlanan/başarısız)
    @Query("SELECT sj FROM SyncJobEntity sj WHERE sj.status IN (:statuses) " +
           "AND sj.systemUpdatedAt < :cleanupThreshold")
    List<SyncJobEntity> findJobsForCleanup(@Param("statuses") List<SyncJobStatus> statuses, 
                                           @Param("cleanupThreshold") OffsetDateTime cleanupThreshold);
}