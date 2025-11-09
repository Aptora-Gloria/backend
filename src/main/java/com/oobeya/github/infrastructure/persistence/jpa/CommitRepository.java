package com.oobeya.github.infrastructure.persistence.jpa;

import com.oobeya.github.infrastructure.persistence.entity.CommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommitRepository extends JpaRepository<CommitEntity, Long> {
    List<CommitEntity> findByRepoName(String repoName);
}
