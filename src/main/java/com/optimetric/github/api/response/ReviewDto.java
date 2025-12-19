package com.optimetric.github.api.response;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object for Pull Request Review information.
 */
public record ReviewDto(
        Long id,
        Long githubReviewId,
        Long pullRequestId,
        ReviewerInfo reviewer,
        String state,
        String body,
        OffsetDateTime submittedAt,
        String htmlUrl
) {
    /**
     * Nested record for reviewer information.
     */
    public record ReviewerInfo(
            String login,
            Long githubId
    ) {}
}