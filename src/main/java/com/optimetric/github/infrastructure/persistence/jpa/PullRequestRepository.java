package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PullRequestRepository extends JpaRepository<PullRequestEntity, Long> {

    List<PullRequestEntity> findByRepository(RepositoryEntity repository);
    
    Optional<PullRequestEntity> findByRepositoryAndGithubPrId(RepositoryEntity repository, Long githubPrId);
    
    Optional<PullRequestEntity> findByRepositoryAndNumber(RepositoryEntity repository, Integer number);
}


