package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.ChangeStatsDto;
import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.api.response.ReviewDto;
import com.optimetric.github.api.response.ReviewCommentDto;
import com.optimetric.github.application.dto.ChangeStatsInfo;
import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.dto.ReviewCommentInfo;
import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.github.domain.enumeration.DataLevel;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommitEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Pull Request entities to DTOs.
 * Implements Requirements 7.1, 7.2, and 7.5 for change statistics availability and data freshness indicators.
 */
public final class PullRequestMapper {

    private PullRequestMapper() {}

    /**
     * Maps PullRequestEntity to PullRequestDto for API response.
     * Implements Requirements 7.1 and 7.2 for change statistics availability logic.
     * Implements Requirement 7.5 for last_synced_at timestamps.
     * 
     * @param entity The pull request entity
     * @return PullRequestDto with proper change statistics availability indicator
     */
    public static PullRequestDto toDto(PullRequestEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        // Requirement 7.1 & 7.2: Create ChangeStatsDto with availability logic
        ChangeStatsDto changeStats = ChangeStatsDto.of(
                entity.getAdditions(),
                entity.getDeletions(),
                entity.getChangedFiles(),
                entity.getChangeStatsSyncedAt()
        );

        return new PullRequestDto(
                entity.getId(),
                entity.getGithubPrId(),
                entity.getPrNumber(),
                entity.getRepository() != null ? entity.getRepository().getFullName() : null,
                entity.getDataLevel() != null ? DataLevel.valueOf(entity.getDataLevel().name()) : null,
                entity.getState(),
                entity.getDraft(),
                entity.getTitle(),
                entity.getBody(),
                entity.getAuthorLogin(),
                entity.getAuthorGithubId(),
                entity.getHtmlUrl(),
                entity.getMergeCommitSha(),
                changeStats,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getClosedAt(),
                entity.getMergedAt(),
                entity.getLastSyncedAt() // Requirement 7.5: Provide last_synced_at timestamp
        );
    }

    /**
     * Maps PullRequestReviewEntity to ReviewDto for API response.
     * 
     * @param entity The pull request review entity
     * @return ReviewDto
     */
    public static ReviewDto toReviewDto(PullRequestReviewEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        ReviewDto.ReviewerInfo reviewerInfo = new ReviewDto.ReviewerInfo(
                entity.getReviewerLogin(),
                entity.getReviewerGithubId()
        );

        return new ReviewDto(
                entity.getId(),
                entity.getGithubReviewId(),
                entity.getPullRequest() != null ? entity.getPullRequest().getId() : null,
                reviewerInfo,
                entity.getState(),
                entity.getBody(),
                entity.getSubmittedAt(),
                entity.getHtmlUrl()
        );
    }

    /**
     * Maps PullRequestCommitEntity to ReviewCommentDto for API response.
     * Note: This follows the existing naming convention where commits are represented as ReviewCommentDto.
     * 
     * @param entity The pull request commit entity
     * @return ReviewCommentDto representing commit information
     */
    public static ReviewCommentDto toCommitDto(PullRequestCommitEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        ReviewCommentDto.CommentAuthorInfo authorInfo = new ReviewCommentDto.CommentAuthorInfo(
                entity.getAuthorLogin(),
                entity.getAuthorName(),
                null // Email not stored in PullRequestCommitEntity
        );

        return new ReviewCommentDto(
                entity.getId(),
                entity.getGithubRepoId(),
                entity.getSha(),
                entity.getPullRequest() != null ? entity.getPullRequest().getId() : null,
                entity.getMessage(),
                authorInfo,
                entity.getCommittedAt(),
                entity.getHtmlUrl()
        );
    }

    /**
     * Maps list of PullRequestEntity to list of PullRequestDto.
     * 
     * @param entities List of pull request entities
     * @return List of PullRequestDto
     */
    public static List<PullRequestDto> toDtos(List<PullRequestEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(PullRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps list of PullRequestReviewEntity to list of ReviewDto.
     * 
     * @param entities List of pull request review entities
     * @return List of ReviewDto
     */
    public static List<ReviewDto> toReviewDtos(List<PullRequestReviewEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(PullRequestMapper::toReviewDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps list of PullRequestCommitEntity to list of ReviewCommentDto.
     * 
     * @param entities List of pull request commit entities
     * @return List of ReviewCommentDto representing commits
     */
    public static List<ReviewCommentDto> toCommitDtos(List<PullRequestCommitEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(PullRequestMapper::toCommitDto)
                .collect(Collectors.toList());
    }

    // --- Application DTO (Port/In response) ---

    public static PullRequestInfo toPullRequestInfo(PullRequestEntity entity) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
        ChangeStatsInfo changeStats = ChangeStatsInfo.of(
                entity.getAdditions(),
                entity.getDeletions(),
                entity.getChangedFiles(),
                entity.getChangeStatsSyncedAt()
        );
        DataLevel dataLevel = entity.getDataLevel() != null ? DataLevel.valueOf(entity.getDataLevel().name()) : null;
        return new PullRequestInfo(
                entity.getId(),
                entity.getGithubPrId(),
                entity.getPrNumber(),
                entity.getRepository() != null ? entity.getRepository().getFullName() : null,
                dataLevel,
                entity.getState(),
                entity.getDraft(),
                entity.getTitle(),
                entity.getBody(),
                entity.getAuthorLogin(),
                entity.getAuthorGithubId(),
                entity.getHtmlUrl(),
                entity.getMergeCommitSha(),
                changeStats,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getClosedAt(),
                entity.getMergedAt(),
                entity.getLastSyncedAt()
        );
    }

    public static List<PullRequestInfo> toPullRequestInfos(List<PullRequestEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(PullRequestMapper::toPullRequestInfo).collect(Collectors.toList());
    }

    public static ReviewInfo toReviewInfo(PullRequestReviewEntity entity) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
        ReviewInfo.ReviewerInfo reviewerInfo = new ReviewInfo.ReviewerInfo(
                entity.getReviewerLogin(),
                entity.getReviewerGithubId()
        );
        return new ReviewInfo(
                entity.getId(),
                entity.getGithubReviewId(),
                entity.getPullRequest() != null ? entity.getPullRequest().getId() : null,
                reviewerInfo,
                entity.getState(),
                entity.getBody(),
                entity.getSubmittedAt(),
                entity.getHtmlUrl()
        );
    }

    public static List<ReviewInfo> toReviewInfos(List<PullRequestReviewEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(PullRequestMapper::toReviewInfo).collect(Collectors.toList());
    }

    public static ReviewCommentInfo toReviewCommentInfo(PullRequestCommitEntity entity) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
        ReviewCommentInfo.CommentAuthorInfo authorInfo = new ReviewCommentInfo.CommentAuthorInfo(
                entity.getAuthorLogin(),
                entity.getAuthorName(),
                null
        );
        return new ReviewCommentInfo(
                entity.getId(),
                entity.getGithubRepoId(),
                entity.getSha(),
                entity.getPullRequest() != null ? entity.getPullRequest().getId() : null,
                entity.getMessage(),
                authorInfo,
                entity.getCommittedAt(),
                entity.getHtmlUrl()
        );
    }

    public static List<ReviewCommentInfo> toReviewCommentInfos(List<PullRequestCommitEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(PullRequestMapper::toReviewCommentInfo).collect(Collectors.toList());
    }
}
