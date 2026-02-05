package com.optimetric.github.api.response;

import java.time.OffsetDateTime;

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
    public record ReviewerInfo(
            String login,
            Long githubId
    ) {}
}