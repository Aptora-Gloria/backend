package com.oobeya.github.infrastructure.persistence.adapter;


import com.oobeya.github.api.response.BranchDto;
import com.oobeya.github.infrastructure.persistence.entity.BranchEntity;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;

import com.oobeya.github.infrastructure.core.mapper.BranchMapper;
import com.oobeya.github.infrastructure.persistence.jpa.BranchRepository;
import com.oobeya.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.BranchServicePort;
import com.oobeya.auth.application.AuthServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceAdapter implements BranchServicePort {

    private final BranchRepository branchRepository;
    private final RepositoryRepository repositoryRepository;
    private final ApiClientPort apiClient;
    private final AuthServicePort authServicePort;

    @Override
    @Transactional
    public List<BranchDto> initRepoBranches(String authToken, long repo_id) {
        String accessToken = authServicePort.getAccessTokenFromToken(authToken);

        RepositoryEntity repo = repositoryRepository.findById(repo_id)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found by repo_id: " + repo_id));

        String owner = repo.getOwnerLogin(); // <<< tek fark burası
        List<String> branchNames = apiClient.getRepoBranchNames(accessToken, owner, repo.getName());

        saveBranches(branchNames, repo);
        return getBranchesFromDb(repo_id);
    }




    @Override
    @Transactional(readOnly = true)
    public List<BranchDto> getBranchesFromDb(long repo_id) {
        return branchRepository.findAllByRepository_Id(repo_id)
                .stream()
                .map(BranchMapper::toDto)
                .toList();
    }


    private void saveBranches(List<String> branchNames, RepositoryEntity repo) {
        if (branchNames == null || branchNames.isEmpty()) return;

        // Mevcut branch'leri repo_id'ye göre getir
        List<BranchEntity> existing = branchRepository.findAllByRepository_Id(repo.getId());

        // Mevcut branch isimlerini bir set'e al (case-insensitive karşılaştırma için)
        var existingBranchNames = existing.stream()
                .map(BranchEntity::getName)
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());

        // Sadece yeni branch'leri kaydet
        List<BranchEntity> toSave = new ArrayList<>();
        for (String name : branchNames) {
            if (name == null || name.isBlank()) continue;
            
            // Aynı isimde branch varsa atla
            if (existingBranchNames.contains(name.toLowerCase())) continue;

            BranchEntity e = BranchEntity.builder()
                    .name(name)
                    .repository(repo)
                    .build();
            toSave.add(e);
        }

        if (!toSave.isEmpty()) {
            branchRepository.saveAll(toSave);
        }
    }
}
