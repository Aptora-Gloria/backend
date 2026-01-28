package com.optimetric.github.infrastructure.persistence.adapter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimetric.github.api.response.RepositoryReadinessDto;
import com.optimetric.github.api.response.RepositoryReadinessFlags;
import com.optimetric.github.application.port.RepoSyncStatusServicePort;
import com.optimetric.github.domain.enumeration.MissingComponent;
import com.optimetric.github.domain.enumeration.ReadinessState;
import com.optimetric.github.infrastructure.persistence.entity.RepositoryEntity;
import com.optimetric.github.infrastructure.persistence.jpa.BranchRepository;
import com.optimetric.github.infrastructure.persistence.jpa.CommitRepository;
import com.optimetric.github.infrastructure.persistence.jpa.LanguageRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestReviewRepository;
import com.optimetric.github.infrastructure.persistence.jpa.RepositoryRepository;
import com.optimetric.shared.exception.RepositoryNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Adapter for repository sync status service.
 * Checks data readiness for repositories by querying database.
 */
@Service
@RequiredArgsConstructor
public class RepoSyncStatusServiceAdapter implements RepoSyncStatusServicePort {

    private final RepositoryRepository repositoryRepository;
    private final BranchRepository branchRepository;
    private final CommitRepository commitRepository;
    private final PullRequestRepository pullRequestRepository;
    private final PullRequestReviewRepository pullRequestReviewRepository;
    private final LanguageRepository languageRepository;

    @Override
    @Transactional(readOnly = true)
    public RepositoryReadinessDto getReadiness(String fullName) throws RepositoryNotFoundException {
        // Find repository by full name
        RepositoryEntity repository = repositoryRepository.findByFullName(fullName)
                .orElseThrow(() -> new RepositoryNotFoundException(
                        "Repository not found with fullName: " + fullName + ". Please initialize the repository first."));

        Long repositoryId = repository.getId();

        // Check each readiness flag
        boolean branchesReady = branchRepository.existsByRepositoryId(repositoryId);
        boolean commitsReady = commitRepository.existsByRepositoryId(repositoryId);
        boolean pullRequestsReady = pullRequestRepository.existsByRepositoryId(repositoryId);
        boolean prDetailsReady = pullRequestRepository.existsWithDetailsByRepositoryId(repositoryId);
        boolean reviewsReady = pullRequestReviewRepository.existsByRepositoryId(repositoryId);
        boolean languagesReady = languageRepository.existsByRepositoryId(repositoryId);

        // Create flags DTO
        RepositoryReadinessFlags flags = new RepositoryReadinessFlags(
                branchesReady,
                commitsReady,
                pullRequestsReady,
                prDetailsReady,
                reviewsReady,
                languagesReady
        );

        // Build missing list in the required order: BRANCHES, PRS, PR_DETAILS, REVIEWS, COMMITS, LANGUAGES
        List<MissingComponent> missing = new ArrayList<>();
        if (!branchesReady) {
            missing.add(MissingComponent.BRANCHES);
        }
        if (!pullRequestsReady) {
            missing.add(MissingComponent.PRS);
        }
        if (!prDetailsReady) {
            missing.add(MissingComponent.PR_DETAILS);
        }
        if (!reviewsReady) {
            missing.add(MissingComponent.REVIEWS);
        }
        if (!commitsReady) {
            missing.add(MissingComponent.COMMITS);
        }
        if (!languagesReady) {
            missing.add(MissingComponent.LANGUAGES);
        }

        // Calculate state
        ReadinessState state;
        if (missing.isEmpty()) {
            state = ReadinessState.READY;
        } else if (missing.size() == 6) {
            // All flags are false
            state = ReadinessState.NOT_READY;
        } else {
            // Some flags are true, some are false
            state = ReadinessState.PARTIAL;
        }

        return new RepositoryReadinessDto(state, missing, flags);
    }
}
