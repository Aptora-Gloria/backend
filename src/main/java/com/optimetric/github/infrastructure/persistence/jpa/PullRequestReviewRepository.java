package com.optimetric.github.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;

public interface PullRequestReviewRepository extends JpaRepository<PullRequestReviewEntity, Long> {

    // İdempotent işlemler için GitHub review id ile bul
    Optional<PullRequestReviewEntity> findByGithubReviewId(Long githubReviewId);

    // Belirli bir PR için tüm incelemeleri getir
    List<PullRequestReviewEntity> findByPullRequestOrderBySubmittedAtAsc(PullRequestEntity pullRequest);

    // PR ve state ile incelemeleri getir
    List<PullRequestReviewEntity> findByPullRequestAndStateOrderBySubmittedAtAsc(PullRequestEntity pullRequest, String state);

    // İnceleyen login ile incelemeleri getir
    List<PullRequestReviewEntity> findByReviewerLoginOrderBySubmittedAtDesc(String reviewerLogin);

    // PR ve reviewer ile incelemeleri getir
    List<PullRequestReviewEntity> findByPullRequestAndReviewerLoginOrderBySubmittedAtAsc(PullRequestEntity pullRequest, String reviewerLogin);

    // PR başına inceleme sayısı
    long countByPullRequest(PullRequestEntity pullRequest);

    // PR ve state ile inceleme sayısı
    long countByPullRequestAndState(PullRequestEntity pullRequest, String state);

    // Find reviews by reviewer GitHub ID
    List<PullRequestReviewEntity> findByReviewerGithubIdOrderBySubmittedAtDesc(Long reviewerGithubId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM PullRequestReviewEntity r " +
           "JOIN r.pullRequest p " +
           "WHERE p.repository.id = :repositoryId")
    boolean existsByRepositoryId(@Param("repositoryId") Long repositoryId);
}