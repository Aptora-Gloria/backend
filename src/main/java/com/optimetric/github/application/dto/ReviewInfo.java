package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

public record ReviewInfo(
        Long id,
        Long githubReviewId,
        Long pullRequestId,
        ReviewerInfo reviewer,
        String state,
        String body,
        OffsetDateTime submittedAt,
        String htmlUrl
) {
    public record ReviewerInfo(
            String login,
            Long githubId
    ) {}
}
