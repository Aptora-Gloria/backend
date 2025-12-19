package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.DataLevel;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PullRequestRepository extends JpaRepository<PullRequestEntity, Long> {

    // Find by GitHub PR ID for idempotent operations
    Optional<PullRequestEntity> findByGithubPrId(Long githubPrId);

    // Find by repository and PR number for API access
    Optional<PullRequestEntity> findByRepositoryAndPrNumber(RepositoryEntity repository, Integer prNumber);

    // Find all PRs for a repository, ordered by creation date
    List<PullRequestEntity> findByRepositoryOrderByCreatedAtDesc(RepositoryEntity repository);

    // Find PRs by repository and state
    List<PullRequestEntity> findByRepositoryAndStateOrderByCreatedAtDesc(RepositoryEntity repository, String state);

    // Find PRs by data level for processing
    List<PullRequestEntity> findByDataLevel(DataLevel dataLevel);

    // Find PRs that need synchronization (updated after last sync)
    @Query("SELECT pr FROM PullRequestEntity pr WHERE pr.repository = :repository " +
           "AND (pr.lastSyncedAt IS NULL OR pr.updatedAt > pr.lastSyncedAt)")
    List<PullRequestEntity> findPullRequestsNeedingSync(@Param("repository") RepositoryEntity repository);

    // Find PRs by repository and data level
    List<PullRequestEntity> findByRepositoryAndDataLevel(RepositoryEntity repository, DataLevel dataLevel);

    // Find PRs updated after a specific timestamp
    List<PullRequestEntity> findByRepositoryAndUpdatedAtAfter(RepositoryEntity repository, OffsetDateTime updatedAfter);

    // Count PRs by repository and state
    long countByRepositoryAndState(RepositoryEntity repository, String state);

    // Find PRs with missing change statistics (for PR_DETAIL job scheduling)
    @Query("SELECT pr FROM PullRequestEntity pr WHERE pr.repository = :repository " +
           "AND (pr.additions IS NULL OR pr.deletions IS NULL OR pr.changedFiles IS NULL)")
    List<PullRequestEntity> findPullRequestsWithoutChangeStats(@Param("repository") RepositoryEntity repository);
}