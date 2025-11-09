package com.oobeya.github.infrastructure.persistence.jpa;

import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositoryRepository extends JpaRepository<RepositoryEntity, Long> {

    List<RepositoryEntity> findAllByOwnerLoginOrderByPushedAtDesc(String ownerLogin);

    List<RepositoryEntity> findAllByHtmlUrlIn(List<String> htmlUrls);
    List<RepositoryEntity> findByGithubRepoId(Long githubRepoId);
    Optional<RepositoryEntity> findByHtmlUrl(String htmlUrl);
}
