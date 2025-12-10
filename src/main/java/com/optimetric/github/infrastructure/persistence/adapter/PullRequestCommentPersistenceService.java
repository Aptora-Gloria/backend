package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.github.api.response.PullRequestCommentDto;
import com.optimetric.github.infrastructure.core.mapper.PullRequestCommentMapper;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommentEntity;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestCommentRepository;
import com.optimetric.github.infrastructure.persistence.jpa.PullRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Persistence service for syncing Pull Request comments.
 * Single responsibility: Save comments and update PR comment count.
 * Note: This service is called from within a transactional context managed by PullRequestMetricsSyncService.
 */
@Service
@RequiredArgsConstructor
public class PullRequestCommentPersistenceService {

    private final PullRequestCommentRepository commentRepository;
    private final PullRequestRepository pullRequestRepository;

    public void syncComments(PullRequestEntity pullRequest, List<PullRequestCommentDto> commentDtos) {
        if (pullRequest == null) {
            return;
        }

        if (commentDtos == null || commentDtos.isEmpty()) {
            pullRequest.setCommentCount(0);
            pullRequestRepository.save(pullRequest);
            return;
        }

        // Get existing comments
        List<PullRequestCommentEntity> existingComments = commentRepository.findByPullRequest(pullRequest);
        Set<Long> existingCommentIds = existingComments.stream()
                .map(PullRequestCommentEntity::getGithubCommentId)
                .collect(Collectors.toSet());

        // Save new comments
        List<PullRequestCommentEntity> toSave = new ArrayList<>();
        for (PullRequestCommentDto dto : commentDtos) {
            if (dto.githubCommentId() == null) {
                continue;
            }

            if (existingCommentIds.contains(dto.githubCommentId())) {
                continue;
            }

            PullRequestCommentEntity entity = PullRequestCommentMapper.toEntity(dto, pullRequest);
            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            commentRepository.saveAll(toSave);
        }

        // Update PR comment count
        pullRequest.setCommentCount(commentDtos.size());
        pullRequestRepository.save(pullRequest);
    }
}

