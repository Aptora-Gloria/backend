package com.oobeya.github.infrastructure.persistence.adapter;

import com.oobeya.auth.application.AuthServicePort;
import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.CommitServicePort;
import com.oobeya.github.application.port.RepoServicePort;
import com.oobeya.github.infrastructure.core.mapper.CommitMapper;
import com.oobeya.github.infrastructure.persistence.entity.BranchEntity;
import com.oobeya.github.infrastructure.persistence.entity.CommitEntity;
import com.oobeya.github.infrastructure.persistence.entity.RepositoryEntity;
import com.oobeya.github.infrastructure.persistence.jpa.BranchRepository;
import com.oobeya.github.infrastructure.persistence.jpa.CommitRepository;
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
    private final BranchRepository branchRepository;
    private final RepoServicePort repoServicePort;

    @Override
    @Transactional
    public List<CommitDto> initCommits(String login, String fullName, String branchName, String since) {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);

        RepositoryEntity repository = repoServicePort.getByFullNameForCurrentUser(login, fullName)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with fullName: " + fullName));

        BranchEntity branch = branchRepository.findByRepositoryAndName(repository, branchName)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with name: " + branchName + " in repository: " + fullName));

        String owner = repository.getOwnerLogin();
        String repoName = repository.getName();
        List<CommitDto> commits = apiClient.getCommits(accessToken, owner, repoName, branchName, since);

        saveCommits(commits, repository, branch);

        return commits;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommitDto> getCommits(String login, String fullName, String branchName) {
        RepositoryEntity repository = repoServicePort.getByFullNameForCurrentUser(login, fullName)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with fullName: " + fullName));

        if (branchName == null || branchName.isBlank()) {
            return commitRepository.findByRepository(repository)
                    .stream()
                    .map(CommitMapper::toDto)
                    .collect(Collectors.toList());
        }

        BranchEntity branch = branchRepository.findByRepositoryAndName(repository, branchName)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with name: " + branchName + " in repository: " + fullName));

        return commitRepository.findByRepositoryAndBranch(repository, branch)
                .stream()
                .map(CommitMapper::toDto)
                .collect(Collectors.toList());
    }

    private void saveCommits(List<CommitDto> commits, RepositoryEntity repository, BranchEntity branch) {
        if (commits == null || commits.isEmpty()) {
            return;
        }

        List<CommitEntity> existingCommits = commitRepository.findByBranch(branch);
        Set<String> existingShasInBranch = existingCommits.stream()
                .map(CommitEntity::getSha)
                .collect(Collectors.toSet());

        List<CommitEntity> toSave = new ArrayList<>();
        for (CommitDto dto : commits) {
            if (dto.sha() == null || dto.sha().isBlank()) {
                continue;
            }

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
