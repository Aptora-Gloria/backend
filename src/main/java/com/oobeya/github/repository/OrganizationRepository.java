package com.oobeya.github.repository;

import com.oobeya.github.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {
    Optional<OrganizationEntity> findByLogin(String login);
}
