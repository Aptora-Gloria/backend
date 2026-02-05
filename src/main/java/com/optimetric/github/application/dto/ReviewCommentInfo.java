package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

public record ReviewCommentInfo(
        Long id,
        Long githubRepoId,
        String sha,
        Long pullRequestId,
        String message,
        CommentAuthorInfo author,
        OffsetDateTime committedAt,
        String htmlUrl
) {
    public record CommentAuthorInfo(
            String login,
            String name,
            String email
    ) {}
}
