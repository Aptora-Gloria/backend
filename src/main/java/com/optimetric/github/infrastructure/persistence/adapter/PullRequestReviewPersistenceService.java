package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.github.api.response.PullRequestReviewDto;
import com.optimetric.github.infrastructure.core.mapper.PullRequestReviewMapper;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Persistence service for syncing Pull Request reviews.
 * Single responsibility: Save reviews and update PR review count.
 * Note: This service is called from within a transactional context managed by PullRequestMetricsSyncService.
 */
@Service
@RequiredArgsConstructor
public class PullRequestReviewPersistenceService {

    private final PullRequestReviewRepository reviewRepository;
    private final PullRequestRepository pullRequestRepository;

    public void syncReviews(PullRequestEntity pullRequest, List<PullRequestReviewDto> reviewDtos) {
        if (pullRequest == null) {
            return;
        }

        if (reviewDtos == null || reviewDtos.isEmpty()) {
            pullRequest.setReviewCount(0);
            pullRequestRepository.save(pullRequest);
            return;
        }

        // Get existing reviews
        List<PullRequestReviewEntity> existingReviews = reviewRepository.findByPullRequest(pullRequest);
        Set<Long> existingReviewIds = existingReviews.stream()
                .map(PullRequestReviewEntity::getGithubReviewId)
                .collect(Collectors.toSet());

        // Save new reviews
        List<PullRequestReviewEntity> toSave = new ArrayList<>();
        for (PullRequestReviewDto dto : reviewDtos) {
            if (dto.githubReviewId() == null) {
                continue;
            }

            if (existingReviewIds.contains(dto.githubReviewId())) {
                continue;
            }

            PullRequestReviewEntity entity = PullRequestReviewMapper.toEntity(dto, pullRequest);
            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            reviewRepository.saveAll(toSave);
        }

        // Update PR review count
        pullRequest.setReviewCount(reviewDtos.size());
        pullRequestRepository.save(pullRequest);
    }
}

