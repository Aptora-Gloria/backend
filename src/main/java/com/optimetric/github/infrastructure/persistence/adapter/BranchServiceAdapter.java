package com.optimetric.github.infrastructure.persistence.adapter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.api.response.BranchDto;
import com.optimetric.github.application.client.BranchClientPort;
import com.optimetric.github.application.port.BranchServicePort;
import com.optimetric.github.application.port.RepoServicePort;
import com.optimetric.github.infrastructure.core.mapper.BranchMapper;
import com.optimetric.github.infrastructure.persistence.entity.BranchEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.BranchRepository;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchServiceAdapter implements BranchServicePort {

    private final BranchRepository branchRepository;
    private final RepoServicePort repoServicePort;
    private final BranchClientPort branchClient;
    private final AuthServicePort authServicePort;

    @Override
    @Transactional
    public List<BranchDto> initRepoBranches(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);
        RepositoryEntity repo = repoServicePort.getByFullNameForCurrentUserOrThrow(login, fullName);

        String owner = repo.getOwnerLogin();
        List<String> branchNames = branchClient.getRepoBranchNames(accessToken, owner, repo.getName());

        saveBranches(branchNames, repo);
        return getBranchesByFullName(login, fullName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchDto> getBranchesByFullName(String login, String fullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repo = repoServicePort.getByFullNameForCurrentUserOrThrow(login, fullName);

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
