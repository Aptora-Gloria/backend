package com.optimetric.github.infrastructure.persistence.jpa;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.optimetric.github.domain.enumeration.DataLevel;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;

public interface PullRequestRepository extends JpaRepository<PullRequestEntity, Long> {

    // İdempotent işlemler için GitHub PR id ile bul
    Optional<PullRequestEntity> findByGithubPrId(Long githubPrId);

    // API erişimi için repo ve PR numarası ile bul
    Optional<PullRequestEntity> findByRepositoryAndPrNumber(RepositoryEntity repository, Integer prNumber);

    // Repo için tüm PR'ları oluşturulma tarihine göre getir
    List<PullRequestEntity> findByRepositoryOrderByCreatedAtDesc(RepositoryEntity repository);

    // Repo ve state ile PR'ları getir
    List<PullRequestEntity> findByRepositoryAndStateOrderByCreatedAtDesc(RepositoryEntity repository, String state);

    // İşleme için data level ile PR'ları getir
    List<PullRequestEntity> findByDataLevel(DataLevel dataLevel);

    // Senkronizasyon gerektiren PR'lar (son sync'ten sonra güncellenen)
    @Query("SELECT pr FROM PullRequestEntity pr WHERE pr.repository = :repository " +
           "AND (pr.lastSyncedAt IS NULL OR pr.updatedAt > pr.lastSyncedAt)")
    List<PullRequestEntity> findPullRequestsNeedingSync(@Param("repository") RepositoryEntity repository);

    // Repo ve data level ile PR'ları getir
    List<PullRequestEntity> findByRepositoryAndDataLevel(RepositoryEntity repository, DataLevel dataLevel);

    // Belirli zamandan sonra güncellenen PR'lar
    List<PullRequestEntity> findByRepositoryAndUpdatedAtAfter(RepositoryEntity repository, OffsetDateTime updatedAfter);

    // Repo ve state ile PR sayısı
    long countByRepositoryAndState(RepositoryEntity repository, String state);

    // Değişiklik istatistiği eksik PR'lar (PR_DETAIL job planlaması için)
    @Query("SELECT pr FROM PullRequestEntity pr WHERE pr.repository = :repository " +
           "AND (pr.additions IS NULL OR pr.deletions IS NULL OR pr.changedFiles IS NULL)")
    List<PullRequestEntity> findPullRequestsWithoutChangeStats(@Param("repository") RepositoryEntity repository);

    @Query("SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END " +
           "FROM PullRequestEntity pr WHERE pr.repository.id = :repositoryId")
    boolean existsByRepositoryId(@Param("repositoryId") Long repositoryId);

    @Query("SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END " +
           "FROM PullRequestEntity pr WHERE pr.repository.id = :repositoryId " +
           "AND pr.additions IS NOT NULL")
    boolean existsWithDetailsByRepositoryId(@Param("repositoryId") Long repositoryId);
}