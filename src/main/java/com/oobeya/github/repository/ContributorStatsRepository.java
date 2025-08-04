package com.oobeya.github.repository;

import com.oobeya.github.entity.ContributorStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContributorStatsRepository extends JpaRepository<ContributorStatsEntity, Long> {
    List<ContributorStatsEntity> findByOwnerLoginAndRepoName(String ownerLogin, String repoName);
}
