package com.optimetric.github.infrastructure.persistence.jpa;

import com.optimetric.github.infrastructure.persistence.entity.BranchEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<BranchEntity, Long> {

    List<BranchEntity> findAllByRepository_Id(Long repositoryId);
    List<BranchEntity> findByRepository(RepositoryEntity repository);
    Optional<BranchEntity> findByRepositoryAndName(RepositoryEntity repository, String name);

}

