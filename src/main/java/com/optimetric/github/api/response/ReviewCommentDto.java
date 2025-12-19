package com.optimetric.github.api.response;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object for Pull Request Commit information.
 * Note: This DTO represents commits, despite the name suggesting comments.
 * This follows the existing naming convention in the codebase.
 */
public record ReviewCommentDto(
        Long id,
        Long githubRepoId,
        String sha,
        Long pullRequestId,
        String message,
        CommentAuthorInfo author,
        OffsetDateTime committedAt,
        String htmlUrl
) {
    /**
     * Nested record for commit author information.
     */
    public record CommentAuthorInfo(
            String login,
            String name,
            String email
    ) {}
}