package com.optimetric.github.api.response;

import com.optimetric.github.infrastructure.persistence.entity.DataLevel;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object for Pull Request information.
 * Contains all necessary data for API responses including data completeness indicators.
 */
public record PullRequestDto(
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
        ChangeStatsDto changeStats,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime closedAt,
        OffsetDateTime mergedAt,
        OffsetDateTime lastSyncedAt
) {}