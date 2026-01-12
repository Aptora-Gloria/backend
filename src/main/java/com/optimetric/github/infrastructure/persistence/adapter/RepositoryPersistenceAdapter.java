package com.optimetric.github.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.auth.infrastructure.persistence.jpa.repository.UserRepository;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.application.port.out.RepositoryPersistencePort;
import com.optimetric.github.infrastructure.core.mapper.RepositoryMapper;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.shared.domain.OwnerType;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Infrastructure adapter for repository persistence operations.
 * Implements RepositoryPersistencePort using JPA RepositoryRepository.
 */
@Component
@RequiredArgsConstructor
public class RepositoryPersistenceAdapter implements RepositoryPersistencePort {

    private final RepositoryRepository repositoryRepository;
    private final UserRepository userRepository;

    @Override
    public List<RepositoryInfo> saveAll(List<RepositoryInitData> repos, Long userId) {
        if (repos == null || repos.isEmpty()) {
            return List.of();
        }

        // Resolve User entity from userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Get incoming githubRepoIds for deduplication (canonical key: githubRepoId)
        List<Long> incomingRepoIds = repos.stream()
                .map(RepositoryInitData::id)
                .filter(id -> id != null)
                .toList();

        // Find existing repositories by githubRepoId (canonical key)
        List<RepositoryEntity> existingEntities = repositoryRepository.findByGithubRepoIdIn(incomingRepoIds);
        java.util.Set<Long> existingRepoIds = existingEntities.stream()
                .map(RepositoryEntity::getGithubRepoId)
                .collect(java.util.stream.Collectors.toSet());

        // Separate new repos and repos to update
        List<RepositoryInitData> newRepos = repos.stream()
                .filter(repo -> repo.id() == null || !existingRepoIds.contains(repo.id()))
                .toList();

        List<RepositoryInitData> reposToUpdate = repos.stream()
                .filter(repo -> repo.id() != null && existingRepoIds.contains(repo.id()))
                .toList();

        // Convert new repos to entities and save
        List<RepositoryEntity> entitiesToSave = RepositoryMapper.toEntities(newRepos);
        entitiesToSave.forEach(e -> e.setUser(user));
        List<RepositoryEntity> savedEntities = repositoryRepository.saveAll(entitiesToSave);

        // Update existing repositories (upsert logic)
        // For existing repos, update metadata but keep githubRepoId as canonical key
        List<RepositoryEntity> updatedEntities = new java.util.ArrayList<>();
        for (RepositoryInitData repoData : reposToUpdate) {
            RepositoryEntity existingEntity = existingEntities.stream()
                    .filter(e -> e.getGithubRepoId().equals(repoData.id()))
                    .findFirst()
                    .orElse(null);
            
            if (existingEntity != null) {
                // Delegate entity update to mapper (mapping responsibility)
                RepositoryMapper.updateEntity(existingEntity, repoData);
                updatedEntities.add(existingEntity);
            }
        }
        
        List<RepositoryEntity> allSavedEntities = new java.util.ArrayList<>(savedEntities);
        if (!updatedEntities.isEmpty()) {
            allSavedEntities.addAll(repositoryRepository.saveAll(updatedEntities));
        }

        // Return all saved/updated repositories as RepositoryInfo
        return RepositoryMapper.toRepositoryInfos(allSavedEntities);
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

