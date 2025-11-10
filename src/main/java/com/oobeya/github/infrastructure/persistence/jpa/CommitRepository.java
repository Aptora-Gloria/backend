package com.oobeya.github.infrastructure.persistence.jpa;

import com.oobeya.github.infrastructure.persistence.entity.CommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommitRepository extends JpaRepository<CommitEntity, Long> {
    
    /**
     * Finds all commits by repository ID.
     */
    List<CommitEntity> findAllByRepository_Id(Long repositoryId);
    
    /**
     * Finds all commits by branch ID.
     */
    List<CommitEntity> findAllByBranch_Id(Long branchId);
    
    /**
     * Finds all commits by repository ID and branch ID.
     */
    List<CommitEntity> findAllByRepository_IdAndBranch_Id(Long repositoryId, Long branchId);
    
    /**
     * Finds commit by SHA and repository ID.
     */
    java.util.Optional<CommitEntity> findByShaAndRepository_Id(String sha, Long repositoryId);
    
    /**
     * Finds commit by SHA and branch ID.
     * Used to check if commit already exists in a specific branch.
     */
    java.util.Optional<CommitEntity> findByShaAndBranch_Id(String sha, Long branchId);
}
