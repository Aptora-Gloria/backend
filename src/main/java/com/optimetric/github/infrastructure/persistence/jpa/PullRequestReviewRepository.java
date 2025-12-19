package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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
}