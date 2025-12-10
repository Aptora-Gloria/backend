package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.github.api.response.PullRequestDetailsDto;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Persistence service for updating Pull Request details.
 * Single responsibility: Update PR metrics from details DTO.
 * Note: This service is called from within a transactional context managed by PullRequestMetricsSyncService.
 */
@Service
@RequiredArgsConstructor
public class PullRequestPersistenceService {

    private final PullRequestRepository pullRequestRepository;

    public void updateDetails(PullRequestEntity pullRequest, PullRequestDetailsDto details) {
        if (pullRequest == null || details == null) {
            return;
        }

        pullRequest.setAdditions(details.additions());
        pullRequest.setDeletions(details.deletions());
        pullRequest.setChangedFiles(details.changedFiles());
        pullRequest.setMerged(details.merged() != null && details.merged());
        pullRequest.setMergedAt(details.mergedAt());

        pullRequestRepository.save(pullRequest);
    }
}

