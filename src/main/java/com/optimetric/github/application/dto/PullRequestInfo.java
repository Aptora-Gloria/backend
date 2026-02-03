package com.optimetric.github.application.dto;

import com.optimetric.github.domain.enumeration.DataLevel;

import java.time.OffsetDateTime;

/**
 * Application DTO for Pull Request information.
 * Used by application and domain layers only; not exposed in API.
 */
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
