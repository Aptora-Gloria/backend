package com.oobeya.github.infrastructure.persistence.jpa;

import com.oobeya.github.infrastructure.persistence.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchRepository extends JpaRepository<BranchEntity, Long> {

    List<BranchEntity> findAllByRepository_Id(Long repositoryId);

}

