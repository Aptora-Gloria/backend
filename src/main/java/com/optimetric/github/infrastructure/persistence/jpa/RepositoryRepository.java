package com.optimetric.github.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.shared.domain.OwnerType;

public interface RepositoryRepository extends JpaRepository<RepositoryEntity, Long> {

    List<RepositoryEntity> findAllByOwnerLoginOrderByPushedAtDesc(String ownerLogin);
    List<RepositoryEntity> findById(int id);

    List<RepositoryEntity> findAllByHtmlUrlIn(List<String> htmlUrls);
    List<RepositoryEntity> findByGithubRepoIdIn(List<Long> githubRepoIds);
    Optional<RepositoryEntity> findByGithubRepoId(Long githubRepoId);
    Optional<RepositoryEntity> findByHtmlUrl(String htmlUrl);

    List<RepositoryEntity> findByUserIdOrderByPushedAtDesc(Long userId);
    List<RepositoryEntity> findByUserIdAndOwnerTypeOrderByPushedAtDesc(Long userId, OwnerType ownerType);
    List<RepositoryEntity> findByUserIdAndOwnerTypeAndOwnerLoginOrderByPushedAtDesc(Long userId, OwnerType ownerType, String ownerLogin);
    Optional<RepositoryEntity> findByFullNameAndUserId(String fullName, Long userId);
    Optional<RepositoryEntity> findByFullName(String fullName);
}
