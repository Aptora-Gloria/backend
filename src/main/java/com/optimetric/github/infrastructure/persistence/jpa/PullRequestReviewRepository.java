package com.optimetric.github.infrastructure.persistence.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;

public interface PullRequestReviewRepository extends JpaRepository<PullRequestReviewEntity, Long> {

    // Find by GitHub review ID for idempotent operations
    Optional<PullRequestReviewEntity> findByGithubReviewId(Long githubReviewId);

    // Find all reviews for a specific pull request
    List<PullRequestReviewEntity> findByPullRequestOrderBySubmittedAtAsc(PullRequestEntity pullRequest);

    // Find reviews by pull request and state
    List<PullRequestReviewEntity> findByPullRequestAndStateOrderBySubmittedAtAsc(PullRequestEntity pullRequest, String state);

    // Find reviews by reviewer login
    List<PullRequestReviewEntity> findByReviewerLoginOrderBySubmittedAtDesc(String reviewerLogin);

    // Find reviews by pull request and reviewer
    List<PullRequestReviewEntity> findByPullRequestAndReviewerLoginOrderBySubmittedAtAsc(PullRequestEntity pullRequest, String reviewerLogin);

    // Count reviews by pull request
    long countByPullRequest(PullRequestEntity pullRequest);

    // Count reviews by pull request and state
    long countByPullRequestAndState(PullRequestEntity pullRequest, String state);

    // Find reviews by reviewer GitHub ID
    List<PullRequestReviewEntity> findByReviewerGithubIdOrderBySubmittedAtDesc(Long reviewerGithubId);

    /**
     * Checks if any review exists for the given repository ID.
     * Uses JOIN with pull_requests table since reviews table doesn't have repository_id.
     * Used for readiness checks (EXISTS with LIMIT 1 for performance).
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM PullRequestReviewEntity r " +
           "JOIN r.pullRequest p " +
           "WHERE p.repository.id = :repositoryId")
    boolean existsByRepositoryId(@Param("repositoryId") Long repositoryId);

    /**
     * Loads all reviews for a repository in one query (avoids N+1 when iterating PRs).
     * Used by metrics adapters instead of findByPullRequestOrderBySubmittedAtAsc per PR.
     */
    @Query("SELECT r FROM PullRequestReviewEntity r JOIN FETCH r.pullRequest p WHERE p.repository.id = :repositoryId")
    List<PullRequestReviewEntity> findByPullRequest_Repository_Id(@Param("repositoryId") Long repositoryId);

    /**
     * Loads all reviews for multiple repositories in one query (avoids N+1 for user-level metrics).
     */
    @Query("SELECT r FROM PullRequestReviewEntity r JOIN FETCH r.pullRequest p WHERE p.repository.id IN :repositoryIds")
    List<PullRequestReviewEntity> findByPullRequest_Repository_IdIn(@Param("repositoryIds") Collection<Long> repositoryIds);
}