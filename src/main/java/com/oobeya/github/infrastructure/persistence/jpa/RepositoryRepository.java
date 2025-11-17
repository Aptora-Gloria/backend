package com.oobeya.github.infrastructure.persistence.jpa;

import com.oobeya.auth.infrastructure.persistence.entity.OwnerType;
import com.oobeya.auth.infrastructure.persistence.entity.User;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositoryRepository extends JpaRepository<RepositoryEntity, Long> {

    List<RepositoryEntity> findAllByOwnerLoginOrderByPushedAtDesc(String ownerLogin);
    List<RepositoryEntity> findById(int id);

    List<RepositoryEntity> findAllByHtmlUrlIn(List<String> htmlUrls);
    List<RepositoryEntity> findByGithubRepoId(Long githubRepoId);
    Optional<RepositoryEntity> findByHtmlUrl(String htmlUrl);

    List<RepositoryEntity> findByUserOrderByPushedAtDesc(User user);
    List<RepositoryEntity> findByUserAndOwnerTypeOrderByPushedAtDesc(User user, OwnerType ownerType);
    List<RepositoryEntity> findByUserAndOwnerTypeAndOwnerLoginOrderByPushedAtDesc(User user, OwnerType ownerType, String ownerLogin);
    Optional<RepositoryEntity> findByFullNameAndUser(String fullName, User user);
}
