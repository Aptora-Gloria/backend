package com.oobeya.github.repository;

import com.oobeya.github.entity.CommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommitRepository extends JpaRepository<CommitEntity, Long> {
    List<CommitEntity> findByRepoName(String repoName);
}
