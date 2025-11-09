package com.oobeya.github.infrastructure.persistence.adapter;

import com.oobeya.auth.application.AuthServicePort;
import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.CommitServicePort;
import com.oobeya.github.infrastructure.core.mapper.CommitMapper;
import com.oobeya.github.infrastructure.persistence.entity.BranchEntity;
import com.oobeya.github.infrastructure.persistence.entity.CommitEntity;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;
import com.oobeya.github.infrastructure.persistence.jpa.BranchRepository;
import com.oobeya.github.infrastructure.persistence.jpa.CommitRepository;
import com.oobeya.github.infrastructure.persistence.jpa.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommitServiceAdapter implements CommitServicePort {

    private final ApiClientPort apiClient;
    private final AuthServicePort authServicePort;
    private final CommitRepository commitRepository;
    private final RepositoryRepository repositoryRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public List<CommitDto> fetchAndSaveCommits(String authHeader, Long repositoryId, Long branchId) {
        String accessToken = authServicePort.getAccessTokenFromToken(authHeader);

        // Get repository and branch entities
        RepositoryEntity repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with id: " + repositoryId));

        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with id: " + branchId));

        // Verify branch belongs to repository
        if (!branch.getRepository().getId().equals(repositoryId)) {
            throw new IllegalArgumentException("Branch does not belong to the specified repository");
        }

        // Fetch commits from GitHub API
        String owner = repository.getOwnerLogin();
        String repoName = repository.getName();
        List<CommitDto> commits = apiClient.getCommits(accessToken, owner, repoName);

        // Save commits to database
        saveCommits(commits, repository, branch);

        return commits;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommitDto> getCommitsByRepositoryId(Long repositoryId) {
        return commitRepository.findAllByRepository_Id(repositoryId)
                .stream()
                .map(CommitMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommitDto> getCommitsByBranchId(Long branchId) {
        return commitRepository.findAllByBranch_Id(branchId)
                .stream()
                .map(CommitMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommitDto> getCommitsByRepositoryAndBranch(Long repositoryId, Long branchId) {
        return commitRepository.findAllByRepository_IdAndBranch_Id(repositoryId, branchId)
                .stream()
                .map(CommitMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Saves commits to database, avoiding duplicates based on (SHA, branch_id) combination.
     * A commit with the same SHA can exist in different branches, but not twice in the same branch.
     */
    private void saveCommits(List<CommitDto> commits, RepositoryEntity repository, BranchEntity branch) {
        if (commits == null || commits.isEmpty()) {
            return;
        }

        // Get existing commit SHAs for this specific branch only
        // Important: Same SHA can exist in different branches, but not in the same branch
        List<CommitEntity> existingCommits = commitRepository.findAllByBranch_Id(branch.getId());
        Set<String> existingShasInBranch = existingCommits.stream()
                .map(CommitEntity::getSha)
                .collect(Collectors.toSet());

        // Filter out duplicates and map to entities
        List<CommitEntity> toSave = new ArrayList<>();
        for (CommitDto dto : commits) {
            if (dto.sha() == null || dto.sha().isBlank()) {
                continue;
            }

            // Skip if commit with this SHA already exists in this branch
            if (existingShasInBranch.contains(dto.sha())) {
                continue;
            }

            CommitEntity entity = CommitMapper.toEntity(dto, repository, branch);
            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            commitRepository.saveAll(toSave);
        }
    }
}
