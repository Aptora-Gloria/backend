package com.oobeya.github.repository;

import com.oobeya.github.entity.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanguageRepository extends JpaRepository<LanguageEntity, Long> {
    List<LanguageEntity> findByRepoName(String repoName);
    void deleteByRepoName(String repoName);
}