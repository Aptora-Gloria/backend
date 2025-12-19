package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.BranchEntity;
import com.optimetric.github.infrastructure.persistence.entity.CommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommitRepository extends JpaRepository<CommitEntity, Long> {
    
    List<CommitEntity> findAllByRepository_Id(Long repositoryId);
    List<CommitEntity> findAllByBranch_Id(Long branchId);
    List<CommitEntity> findAllByRepository_IdAndBranch_Id(Long repositoryId, Long branchId);
    Optional<CommitEntity> findByShaAndRepository_Id(String sha, Long repositoryId);
    Optional<CommitEntity> findByShaAndBranch_Id(String sha, Long branchId);
    
    List<CommitEntity> findByRepository(RepositoryEntity repository);
    List<CommitEntity> findByBranch(BranchEntity branch);
    List<CommitEntity> findByRepositoryAndBranch(RepositoryEntity repository, BranchEntity branch);
    Optional<CommitEntity> findByShaAndRepository(String sha, RepositoryEntity repository);
    Optional<CommitEntity> findByShaAndBranch(String sha, BranchEntity branch);
}
