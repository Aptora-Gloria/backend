package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

public record PullRequestDetailFromApi(
        Long id,
        Integer number,
        String state,
        Boolean draft,
        String title,
        String body,
        String htmlUrl,
        String mergeCommitSha,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime closedAt,
        OffsetDateTime mergedAt,
        PullRequestFromApi.AuthorInfo user,
        Integer additions,
        Integer deletions,
        Integer changedFiles
) {}
