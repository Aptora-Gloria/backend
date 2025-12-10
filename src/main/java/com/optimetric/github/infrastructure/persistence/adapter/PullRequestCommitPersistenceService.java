package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.github.api.response.CommitDto;
import com.optimetric.github.infrastructure.core.mapper.CommitMapper;
import com.optimetric.github.infrastructure.persistence.entity.BranchEntity;
import com.optimetric.github.infrastructure.persistence.entity.CommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.BranchRepository;
import com.optimetric.github.infrastructure.persistence.jpa.CommitRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Persistence service for syncing Pull Request commits.
 * Single responsibility: Match commits and establish PR-commit relationships.
 * Note: This service is called from within a transactional context managed by PullRequestMetricsSyncService.
 */
@Service
@RequiredArgsConstructor
public class PullRequestCommitPersistenceService {

    private final CommitRepository commitRepository;
    private final BranchRepository branchRepository;
    private final PullRequestRepository pullRequestRepository;

    public void syncCommits(
            PullRequestEntity pullRequest,
            List<CommitDto> commitDtos,
            RepositoryEntity repository
    ) {
        if (pullRequest == null) {
            return;
        }
        
        if (commitDtos == null || commitDtos.isEmpty()) {
            pullRequest.setCommitCount(0);
            pullRequestRepository.save(pullRequest);
            return;
        }

        // Get or create branch for PR head
        BranchEntity branch = findOrCreateBranch(
                repository,
                pullRequest.getHeadRef(),
                pullRequest.getHeadSha()
        );

        // Match existing commits or create new ones
        Set<CommitEntity> prCommits = new HashSet<>();
        for (CommitDto commitDto : commitDtos) {
            CommitEntity commit = findOrCreateCommit(commitDto, repository, branch);
            if (commit != null) {
                prCommits.add(commit);
            }
        }

        // Update PR-commit relationship
        pullRequest.setCommits(prCommits);
        pullRequest.setCommitCount(prCommits.size());

        // Save PR to persist the relationship
        pullRequestRepository.save(pullRequest);
    }

    private BranchEntity findOrCreateBranch(RepositoryEntity repository, String branchName, String sha) {
        if (branchName == null || branchName.isBlank()) {
            return null;
        }

        return branchRepository.findByRepositoryAndName(repository, branchName)
                .orElseGet(() -> {
                    BranchEntity newBranch = BranchEntity.builder()
                            .name(branchName)
                            .repository(repository)
                            .build();
                    return branchRepository.save(newBranch);
                });
    }

    private CommitEntity findOrCreateCommit(
            CommitDto commitDto,
            RepositoryEntity repository,
            BranchEntity branch
    ) {
        if (commitDto == null || commitDto.sha() == null) {
            return null;
        }

        // Try to find existing commit by SHA and repository
        return commitRepository.findByShaAndRepository(commitDto.sha(), repository)
                .orElseGet(() -> {
                    CommitEntity newCommit = CommitMapper.toEntity(commitDto, repository, branch);
                    return commitRepository.save(newCommit);
                });
    }
}

