package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.api.response.CommitDto;
import com.optimetric.github.application.client.CommitClientPort;
import com.optimetric.github.application.port.CommitServicePort;
import com.optimetric.github.application.port.RepoServicePort;
import com.optimetric.github.infrastructure.core.mapper.CommitMapper;
import com.optimetric.shared.exception.BranchNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import com.optimetric.github.infrastructure.persistence.entity.BranchEntity;
import com.optimetric.github.infrastructure.persistence.entity.CommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.BranchRepository;
import com.optimetric.github.infrastructure.persistence.jpa.CommitRepository;
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

    private final CommitClientPort commitClient;
    private final AuthServicePort authServicePort;
    private final CommitRepository commitRepository;
    private final BranchRepository branchRepository;
    private final RepoServicePort repoServicePort;

    @Override
    @Transactional
    public List<CommitDto> initCommits(String login, String fullName, String branchName, String since) 
            throws RepositoryNotFoundException, BranchNotFoundException, UserNotFoundException {
        String accessToken = authServicePort.getAccessTokenFromLogin(login);
        RepositoryEntity repository = repoServicePort.getByFullNameForCurrentUserOrThrow(login, fullName);
        BranchEntity branch = findBranchOrThrow(repository, branchName, fullName);

        String owner = repository.getOwnerLogin();
        String repoName = repository.getName();
        List<CommitDto> commits = commitClient.getCommits(accessToken, owner, repoName, branchName, since);

        saveCommits(commits, repository, branch);

        return commits;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommitDto> getCommits(String login, String fullName, String branchName) 
            throws RepositoryNotFoundException, BranchNotFoundException, UserNotFoundException {
        RepositoryEntity repository = repoServicePort.getByFullNameForCurrentUserOrThrow(login, fullName);

        if (branchName == null || branchName.isBlank()) {
            List<CommitEntity> commits = commitRepository.findByRepository(repository);
            return CommitMapper.toDtos(commits);
        }

        BranchEntity branch = findBranchOrThrow(repository, branchName, fullName);
        List<CommitEntity> commits = commitRepository.findByRepositoryAndBranch(repository, branch);
        return CommitMapper.toDtos(commits);
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

    private BranchEntity findBranchOrThrow(RepositoryEntity repository, String branchName, String fullName) 
            throws BranchNotFoundException {
        return branchRepository.findByRepositoryAndName(repository, branchName)
                .orElseThrow(() -> new BranchNotFoundException(
                        "Branch not found with name: " + branchName + " in repository: " + fullName));
    }
}
