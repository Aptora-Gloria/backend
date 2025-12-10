package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PullRequestReviewRepository extends JpaRepository<PullRequestReviewEntity, Long> {
    
    List<PullRequestReviewEntity> findByPullRequest(PullRequestEntity pullRequest);
    
    Optional<PullRequestReviewEntity> findByPullRequestAndGithubReviewId(
            PullRequestEntity pullRequest,
            Long githubReviewId
    );
}

