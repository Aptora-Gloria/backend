package com.optimetric.github.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.application.port.out.RepositoryPersistencePort;
import com.optimetric.github.infrastructure.core.mapper.RepositoryMapper;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.shared.domain.OwnerType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RepositoryPersistenceAdapter implements RepositoryPersistencePort {

    private final RepositoryRepository repositoryRepository;

    @Override
    public List<RepositoryInfo> saveAll(List<RepositoryInitData> repos, Long userId) {
        if (repos == null || repos.isEmpty()) {
            return List.of();
        }

        // Gelen githubRepoId'leri tekilleştirme için al (canonical key: githubRepoId)
        List<Long> incomingRepoIds = repos.stream()
                .map(RepositoryInitData::id)
                .filter(id -> id != null)
                .toList();

        // Find existing repositories by githubRepoId (canonical key)
        List<RepositoryEntity> existingEntities = repositoryRepository.findByGithubRepoIdIn(incomingRepoIds);
        java.util.Set<Long> existingRepoIds = existingEntities.stream()
                .map(RepositoryEntity::getGithubRepoId)
                .collect(java.util.stream.Collectors.toSet());

        // Yeni repolar ile güncellenecek repoları ayır
        List<RepositoryInitData> newRepos = repos.stream()
                .filter(repo -> repo.id() == null || !existingRepoIds.contains(repo.id()))
                .toList();

        List<RepositoryInitData> reposToUpdate = repos.stream()
                .filter(repo -> repo.id() != null && existingRepoIds.contains(repo.id()))
                .toList();

        // Yeni repoları entity'ye çevirip kaydet (userId uygulama katmanından)
        List<RepositoryEntity> entitiesToSave = RepositoryMapper.toEntities(newRepos);
        entitiesToSave.forEach(e -> e.setUserId(userId));
        List<RepositoryEntity> savedEntities = repositoryRepository.saveAll(entitiesToSave);

        // Update existing repositories (upsert logic)
        // Mevcut repolarda metadata güncelle, githubRepoId canonical key kalsın
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
    public List<RepositoryInfo> findByUserWithCriteria(Long userId, OwnerType ownerType, String ownerLogin) {
        List<RepositoryEntity> entities;

        if (ownerType != null && ownerLogin != null) {
            entities = repositoryRepository.findByUserIdAndOwnerTypeAndOwnerLoginOrderByPushedAtDesc(userId, ownerType, ownerLogin);
        } else if (ownerType != null) {
            entities = repositoryRepository.findByUserIdAndOwnerTypeOrderByPushedAtDesc(userId, ownerType);
        } else {
            entities = repositoryRepository.findByUserIdOrderByPushedAtDesc(userId);
        }

        return RepositoryMapper.toRepositoryInfos(entities);
    }

    @Override
    public Optional<RepositoryInfo> findByFullNameAndUser(String fullName, Long userId) {
        Optional<RepositoryEntity> entity = repositoryRepository.findByFullNameAndUserId(fullName, userId);
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

    public Optional<RepositoryEntity> findEntityById(Long id) {
        return repositoryRepository.findById(id);
    }

    public Optional<RepositoryEntity> findEntityByFullName(String fullName) {
        return repositoryRepository.findByFullName(fullName);
    }
}

