package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.application.port.RepositoryPersistencePort;
import com.optimetric.github.infrastructure.core.mapper.RepositoryMapper;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.shared.domain.OwnerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Infrastructure adapter for repository persistence operations.
 * Implements RepositoryPersistencePort using JPA RepositoryRepository.
 */
@Component
@RequiredArgsConstructor
public class RepositoryPersistenceAdapter implements RepositoryPersistencePort {

    private final RepositoryRepository repositoryRepository;

    @Override
    public List<RepositoryInfo> saveAll(List<RepositoryInitData> repos, User user) {
        if (repos == null || repos.isEmpty()) {
            return List.of();
        }

        // Get incoming URLs for deduplication
        List<String> incomingUrls = repos.stream()
                .map(RepositoryInitData::htmlUrl)
                .toList();

        // Find existing repositories by HTML URLs
        List<RepositoryEntity> existingEntities = repositoryRepository.findAllByHtmlUrlIn(incomingUrls);
        List<String> existingUrls = existingEntities.stream()
                .map(RepositoryEntity::getHtmlUrl)
                .toList();

        // Filter out repositories that already exist
        List<RepositoryInitData> newRepos = repos.stream()
                .filter(repo -> !existingUrls.contains(repo.htmlUrl()))
                .toList();

        // Convert to entities and save
        List<RepositoryEntity> entitiesToSave = RepositoryMapper.toEntities(newRepos);
        entitiesToSave.forEach(e -> e.setUser(user));
        List<RepositoryEntity> savedEntities = repositoryRepository.saveAll(entitiesToSave);

        // Return saved repositories as RepositoryInfo
        return RepositoryMapper.toRepositoryInfos(savedEntities);
    }

    @Override
    public List<RepositoryInfo> findAllByOwnerLoginOrderByPushedAtDesc(String ownerLogin) {
        List<RepositoryEntity> entities = repositoryRepository.findAllByOwnerLoginOrderByPushedAtDesc(ownerLogin);
        return RepositoryMapper.toRepositoryInfos(entities);
    }

    @Override
    public List<RepositoryInfo> findByUserWithCriteria(User user, OwnerType ownerType, String ownerLogin) {
        List<RepositoryEntity> entities;
        
        if (ownerType != null && ownerLogin != null) {
            entities = repositoryRepository.findByUserAndOwnerTypeAndOwnerLoginOrderByPushedAtDesc(user, ownerType, ownerLogin);
        } else if (ownerType != null) {
            entities = repositoryRepository.findByUserAndOwnerTypeOrderByPushedAtDesc(user, ownerType);
        } else {
            entities = repositoryRepository.findByUserOrderByPushedAtDesc(user);
        }
        
        return RepositoryMapper.toRepositoryInfos(entities);
    }

    @Override
    public Optional<RepositoryInfo> findByFullNameAndUser(String fullName, User user) {
        Optional<RepositoryEntity> entity = repositoryRepository.findByFullNameAndUser(fullName, user);
        return entity.map(RepositoryMapper::toRepositoryInfo);
    }

    @Override
    public Optional<RepositoryInfo> findById(Long id) {
        return repositoryRepository.findById(id)
                .map(RepositoryMapper::toRepositoryInfo);
    }

    @Override
    public List<RepositoryInfo> findAllByHtmlUrlIn(List<String> htmlUrls) {
        List<RepositoryEntity> entities = repositoryRepository.findAllByHtmlUrlIn(htmlUrls);
        return RepositoryMapper.toRepositoryInfos(entities);
    }

    @Override
    public Optional<RepositoryInfo> findByFullName(String fullName) {
        Optional<RepositoryEntity> entity = repositoryRepository.findByFullName(fullName);
        return entity.map(RepositoryMapper::toRepositoryInfo);
    }

    /**
     * Internal method for infrastructure adapters that need RepositoryEntity.
     * This method should only be used within infrastructure layer adapters that need
     * entity access for JPA operations (e.g., BranchRepository, CommitRepository).
     * 
     * @param id Repository ID
     * @return Optional RepositoryEntity
     */
    public Optional<RepositoryEntity> findEntityById(Long id) {
        return repositoryRepository.findById(id);
    }

    /**
     * Internal method for infrastructure adapters that need RepositoryEntity by full name.
     * This method should only be used within infrastructure layer adapters that need
     * entity access for JPA operations.
     * 
     * @param fullName Repository full name
     * @return Optional RepositoryEntity
     */
    public Optional<RepositoryEntity> findEntityByFullName(String fullName) {
        return repositoryRepository.findByFullName(fullName);
    }
}

