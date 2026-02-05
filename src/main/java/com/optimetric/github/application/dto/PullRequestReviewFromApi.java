package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

public record PullRequestReviewFromApi(
        Long id,
        AuthorInfo user,
        String state,
        String body,
        OffsetDateTime submittedAt,
        String htmlUrl
) {
    public record AuthorInfo(Long id, String login) {}
}
