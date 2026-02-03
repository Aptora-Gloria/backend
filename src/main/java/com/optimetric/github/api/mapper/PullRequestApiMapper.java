package com.optimetric.github.api.mapper;

import com.optimetric.github.api.response.ChangeStatsDto;
import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.api.response.ReviewCommentDto;
import com.optimetric.github.api.response.ReviewDto;
import com.optimetric.github.api.response.SyncStatusDto;
import com.optimetric.github.application.dto.ChangeStatsInfo;
import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.dto.ReviewCommentInfo;
import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.github.application.dto.SyncStatusInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Application DTOs to API DTOs for Pull Request endpoints.
 * Used only in API (controller) layer – API sadece al/ver + bu mapper.
 */
public final class PullRequestApiMapper {

    private PullRequestApiMapper() {}

    public static PullRequestDto toPullRequestDto(PullRequestInfo info) {
        if (info == null) return null;
        ChangeStatsDto changeStats = info.changeStats() != null
                ? new ChangeStatsDto(
                info.changeStats().additions(),
                info.changeStats().deletions(),
                info.changeStats().changedFiles(),
                info.changeStats().available(),
                info.changeStats().syncedAt())
                : null;
        return new PullRequestDto(
                info.id(),
                info.githubPrId(),
                info.prNumber(),
                info.repoFullName(),
                info.dataLevel(),
                info.state(),
                info.draft(),
                info.title(),
                info.body(),
                info.authorLogin(),
                info.authorGithubId(),
                info.htmlUrl(),
                info.mergeCommitSha(),
                changeStats,
                info.createdAt(),
                info.updatedAt(),
                info.closedAt(),
                info.mergedAt(),
                info.lastSyncedAt()
        );
    }

    public static List<PullRequestDto> toPullRequestDtos(List<PullRequestInfo> list) {
        if (list == null) return List.of();
        return list.stream().map(PullRequestApiMapper::toPullRequestDto).collect(Collectors.toList());
    }

    public static ReviewDto toReviewDto(ReviewInfo info) {
        if (info == null) return null;
        ReviewDto.ReviewerInfo reviewerInfo = info.reviewer() != null
                ? new ReviewDto.ReviewerInfo(info.reviewer().login(), info.reviewer().githubId())
                : null;
        return new ReviewDto(
                info.id(),
                info.githubReviewId(),
                info.pullRequestId(),
                reviewerInfo,
                info.state(),
                info.body(),
                info.submittedAt(),
                info.htmlUrl()
        );
    }

    public static List<ReviewDto> toReviewDtos(List<ReviewInfo> list) {
        if (list == null) return List.of();
        return list.stream().map(PullRequestApiMapper::toReviewDto).collect(Collectors.toList());
    }

    public static ReviewCommentDto toReviewCommentDto(ReviewCommentInfo info) {
        if (info == null) return null;
        ReviewCommentDto.CommentAuthorInfo authorInfo = info.author() != null
                ? new ReviewCommentDto.CommentAuthorInfo(
                info.author().login(),
                info.author().name(),
                info.author().email())
                : null;
        return new ReviewCommentDto(
                info.id(),
                info.githubRepoId(),
                info.sha(),
                info.pullRequestId(),
                info.message(),
                authorInfo,
                info.committedAt(),
                info.htmlUrl()
        );
    }

    public static List<ReviewCommentDto> toReviewCommentDtos(List<ReviewCommentInfo> list) {
        if (list == null) return List.of();
        return list.stream().map(PullRequestApiMapper::toReviewCommentDto).collect(Collectors.toList());
    }

    public static SyncStatusDto toSyncStatusDto(SyncStatusInfo info) {
        if (info == null) return null;
        List<SyncStatusDto.JobStatusDto> jobs = info.jobs() == null ? List.of() : info.jobs().stream()
                .map(j -> new SyncStatusDto.JobStatusDto(
                        j.jobType(),
                        j.status(),
                        j.attemptCount(),
                        j.errorMessage(),
                        j.startedAt(),
                        j.completedAt(),
                        j.nextRunAt()))
                .collect(Collectors.toList());
        return new SyncStatusDto(
                info.repoFullName(),
                info.overallStatus(),
                jobs,
                info.lastSyncedAt()
        );
    }
}
