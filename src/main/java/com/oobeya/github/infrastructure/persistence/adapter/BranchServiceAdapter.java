package com.oobeya.github.infrastructure.persistence.adapter;

import com.oobeya.github.api.response.BranchDto;
import com.oobeya.github.infrastructure.persistence.entity.BranchEntity;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;
import com.oobeya.github.infrastructure.core.mapper.BranchMapper;
import com.oobeya.github.infrastructure.persistence.jpa.BranchRepository;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.BranchServicePort;
import com.oobeya.github.application.port.RepoServicePort;
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
    private final RepoServicePort repoServicePort;
    private final ApiClientPort apiClient;
    private final AuthServicePort authServicePort;

    @Override
    @Transactional
    public List<BranchDto> initRepoBranches(String login, String fullName) {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);

        RepositoryEntity repo = repoServicePort.getByFullNameForCurrentUser(login, fullName)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with fullName: " + fullName));

        String owner = repo.getOwnerLogin();
        List<String> branchNames = apiClient.getRepoBranchNames(accessToken, owner, repo.getName());

        saveBranches(branchNames, repo);
        return getBranchesByFullName(login, fullName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchDto> getBranchesByFullName(String login, String fullName) {
        RepositoryEntity repo = repoServicePort.getByFullNameForCurrentUser(login, fullName)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with fullName: " + fullName));

        return branchRepository.findByRepository(repo)
                .stream()
                .map(BranchMapper::toDto)
                .toList();
    }

    private void saveBranches(List<String> branchNames, RepositoryEntity repo) {
        if (branchNames == null || branchNames.isEmpty()) return;

        List<BranchEntity> existing = branchRepository.findByRepository(repo);

        var existingBranchNames = existing.stream()
                .map(BranchEntity::getName)
                .collect(java.util.stream.Collectors.toSet());

        List<BranchEntity> toSave = new ArrayList<>();
        for (String name : branchNames) {
            if (name == null || name.isBlank()) continue;
            
            if (existingBranchNames.contains(name)) continue;

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
