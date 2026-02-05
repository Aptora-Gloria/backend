package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

import com.optimetric.github.domain.enumeration.DataLevel;

public record PullRequestInfo(
        Long id,
        Long githubPrId,
        Integer prNumber,
        String repoFullName,
        DataLevel dataLevel,
        String state,
        Boolean draft,
        String title,
        String body,
        String authorLogin,
        Long authorGithubId,
        String htmlUrl,
        String mergeCommitSha,
        ChangeStatsInfo changeStats,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime closedAt,
        OffsetDateTime mergedAt,
        OffsetDateTime lastSyncedAt
) {}
