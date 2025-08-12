package com.oobeya.github.repository;

import com.oobeya.auth.entity.OwnerType;
import com.oobeya.github.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositoryRepository extends JpaRepository<RepositoryEntity, Long> {
    List<RepositoryEntity> findByLogin(String login);
    List<RepositoryEntity> findAllByHtmlUrlIn(List<String> htmlUrls);
}
