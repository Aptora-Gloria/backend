package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PullRequestCommitRepository extends JpaRepository<PullRequestCommitEntity, Long> {

    // İdempotent işlemler için (github_repo_id, sha) ile bul
    Optional<PullRequestCommitEntity> findByGithubRepoIdAndSha(Long githubRepoId, String sha);

    // Belirli bir PR için tüm commit'leri getir
    List<PullRequestCommitEntity> findByPullRequestOrderByCommittedAtAsc(PullRequestEntity pullRequest);

    // PR ve yazar ile commit'leri getir
    List<PullRequestCommitEntity> findByPullRequestAndAuthorLoginOrderByCommittedAtAsc(PullRequestEntity pullRequest, String authorLogin);

    // SHA ile commit'leri getir (tüm repolarda)
    List<PullRequestCommitEntity> findBySha(String sha);

    // GitHub repo id ile commit'leri getir
    List<PullRequestCommitEntity> findByGithubRepoIdOrderByCommittedAtDesc(Long githubRepoId);

    // Yazar login ile commit'leri getir
    List<PullRequestCommitEntity> findByAuthorLoginOrderByCommittedAtDesc(String authorLogin);

    // PR başına commit sayısı
    long countByPullRequest(PullRequestEntity pullRequest);

    // PR ve yazar başına commit sayısı
    long countByPullRequestAndAuthorLogin(PullRequestEntity pullRequest, String authorLogin);

    // Yazar e-posta ile commit'leri getir
    List<PullRequestCommitEntity> findByAuthorEmailOrderByCommittedAtDesc(String authorEmail);
}