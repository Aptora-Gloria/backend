package com.optimetric.github.infrastructure.core.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.optimetric.github.application.dto.ChangeStatsInfo;
import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.dto.ReviewCommentInfo;
import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.github.domain.enumeration.DataLevel;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestCommitEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestEntity;
import com.optimetric.github.infrastructure.persistence.entity.PullRequestReviewEntity;

public final class PullRequestMapper {

    private PullRequestMapper() {}

    public static PullRequestInfo toPullRequestInfo(PullRequestEntity entity) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
        ChangeStatsInfo changeStats = ChangeStatsInfo.of(
                entity.getAdditions(),
                entity.getDeletions(),
                entity.getChangedFiles(),
                entity.getChangeStatsSyncedAt()
        );
        DataLevel dataLevel = entity.getDataLevel();
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
