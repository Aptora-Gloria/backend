package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

public record PullRequestFromApi(
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
        AuthorInfo user
) {
    public record AuthorInfo(Long id, String login) {}
}
