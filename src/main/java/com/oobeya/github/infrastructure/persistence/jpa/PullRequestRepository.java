package com.oobeya.github.infrastructure.persistence.jpa;

import com.oobeya.github.infrastructure.persistence.entity.PullRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PullRequestRepository extends JpaRepository<PullRequestEntity, Long> {

    Page<PullRequestEntity> findByRepositoryId(Long repositoryId, Pageable pageable);
    
    Page<PullRequestEntity> findByRepositoryIdAndStatus(
            Long repositoryId,
            PullRequestEntity.PullRequestStatus status,
            Pageable pageable
    );
    
    Optional<PullRequestEntity> findByRepositoryIdAndNumber(Long repositoryId, Integer number);
    
    Optional<PullRequestEntity> findByGithubPrId(Long githubPrId);
    
    List<PullRequestEntity> findByRepositoryIdAndStatus(
            Long repositoryId,
            PullRequestEntity.PullRequestStatus status
    );
}



