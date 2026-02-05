package com.optimetric.github.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.optimetric.github.infrastructure.persistence.entity.BranchEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;

public interface BranchRepository extends JpaRepository<BranchEntity, Long> {

    List<BranchEntity> findAllByRepository_Id(Long repositoryId);
    List<BranchEntity> findByRepository(RepositoryEntity repository);
    Optional<BranchEntity> findByRepositoryAndName(RepositoryEntity repository, String name);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM BranchEntity b WHERE b.repository.id = :repositoryId")
    boolean existsByRepositoryId(@Param("repositoryId") Long repositoryId);

}

