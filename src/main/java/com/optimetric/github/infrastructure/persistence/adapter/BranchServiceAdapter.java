package com.optimetric.github.infrastructure.persistence.adapter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.github.application.port.out.BranchClientPort;
import com.optimetric.github.application.port.out.BranchQueryPort;
import com.optimetric.github.application.usecase.GetRepositoryForCurrentUserUseCase;
import com.optimetric.github.infrastructure.core.mapper.BranchMapper;
import com.optimetric.github.infrastructure.persistence.entity.BranchEntity;
import com.optimetric.github.infrastructure.persistence.entity.CommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.BranchRepository;
import com.optimetric.github.infrastructure.persistence.jpa.CommitRepository;
import com.optimetric.github.infrastructure.persistence.adapter.RepositoryPersistenceAdapter;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchServiceAdapter implements BranchQueryPort {

    private final BranchRepository branchRepository;
    private final CommitRepository commitRepository;
    private final RepositoryPersistenceAdapter repositoryPersistenceAdapter;
    private final GetRepositoryForCurrentUserUseCase getRepositoryForCurrentUserUseCase;
    private final BranchClientPort branchClient;
    private final AuthServicePort authServicePort;

    @Override
    @Transactional
    public List<BranchInfo> initRepoBranches(String login, String fullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);
        com.optimetric.github.application.dto.RepositoryInfo repoInfo = getRepositoryForCurrentUserUseCase.executeOrThrow(login, fullName);
        RepositoryEntity repo = repositoryPersistenceAdapter.findEntityById(repoInfo.id())
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with id: " + repoInfo.id()));

        String owner = repo.getOwnerLogin();
        List<String> branchNames = branchClient.getRepoBranchNames(accessToken, owner, repo.getName());

        saveBranches(branchNames, repo);
        return getBranchInfosByRepository(repo);
    }

    private List<BranchInfo> getBranchInfosByRepository(RepositoryEntity repo) {
        List<BranchEntity> branches = branchRepository.findByRepository(repo);
        return branches.stream()
                .map(this::toBranchInfoWithCommitStats)
                .toList();
    }

    private BranchInfo toBranchInfoWithCommitStats(BranchEntity branch) {
        List<CommitEntity> commits = commitRepository.findByBranch(branch);
        long count = commits.size();
        OffsetDateTime lastCommitDate = commits.stream()
                .map(CommitEntity::getCommittedAt)
                .filter(Objects::nonNull)
                .max(OffsetDateTime::compareTo)
                .orElse(null);
        return BranchMapper.toBranchInfo(branch, count, lastCommitDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchInfo> getBranchesByFullName(String login, String fullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        com.optimetric.github.application.dto.RepositoryInfo repoInfo = getRepositoryForCurrentUserUseCase.executeOrThrow(login, fullName);
        RepositoryEntity repo = repositoryPersistenceAdapter.findEntityById(repoInfo.id())
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with id: " + repoInfo.id()));
        List<BranchEntity> branches = branchRepository.findByRepository(repo);
        return branches.stream()
                .map(this::toBranchInfoWithCommitStats)
                .toList();
    }

    private void    saveBranches(List<String> branchNames, RepositoryEntity repo) {
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
