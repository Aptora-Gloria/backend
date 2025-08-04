package com.oobeya.github.repository;

import com.oobeya.github.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryRepository extends JpaRepository<RepositoryEntity, Long> {}
