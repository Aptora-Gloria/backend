package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PullRequestCommitRepository extends JpaRepository<PullRequestCommitEntity, Long> {

    // Find by composite unique key (github_repo_id, sha) for idempotent operations
    Optional<PullRequestCommitEntity> findByGithubRepoIdAndSha(Long githubRepoId, String sha);

    // Find all commits for a specific pull request
    List<PullRequestCommitEntity> findByPullRequestOrderByCommittedAtAsc(PullRequestEntity pullRequest);

    // Find commits by pull request and author
    List<PullRequestCommitEntity> findByPullRequestAndAuthorLoginOrderByCommittedAtAsc(PullRequestEntity pullRequest, String authorLogin);

    // Find commits by SHA (across all repos)
    List<PullRequestCommitEntity> findBySha(String sha);

    // Find commits by GitHub repository ID
    List<PullRequestCommitEntity> findByGithubRepoIdOrderByCommittedAtDesc(Long githubRepoId);

    // Find commits by author login
    List<PullRequestCommitEntity> findByAuthorLoginOrderByCommittedAtDesc(String authorLogin);

    // Count commits by pull request
    long countByPullRequest(PullRequestEntity pullRequest);

    // Count commits by pull request and author
    long countByPullRequestAndAuthorLogin(PullRequestEntity pullRequest, String authorLogin);

    // Find commits by author email
    List<PullRequestCommitEntity> findByAuthorEmailOrderByCommittedAtDesc(String authorEmail);
}