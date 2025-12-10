package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommentEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PullRequestCommentRepository extends JpaRepository<PullRequestCommentEntity, Long> {
    
    List<PullRequestCommentEntity> findByPullRequest(PullRequestEntity pullRequest);
    
    Optional<PullRequestCommentEntity> findByPullRequestAndGithubCommentId(
            PullRequestEntity pullRequest,
            Long githubCommentId
    );
}

