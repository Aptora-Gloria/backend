package com.oobeya.github.repository;

import com.oobeya.github.entity.ContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<ContentEntity, Long> {
    List<ContentEntity> findByRepoName(String repoName);

}
