package com.optimetric.github.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * DTO for GitHub Pull Request Review API response.
 */
public record PullRequestReviewDto(
        @JsonProperty("id") Long githubReviewId,
        @JsonProperty("state") String state,
        @JsonProperty("body") String body,
        @JsonProperty("submitted_at") OffsetDateTime submittedAt,
        @JsonProperty("user") AuthorInfo author
) {
    public record AuthorInfo(
            @JsonProperty("id") Long githubId,
            @JsonProperty("login") String login
    ) {}
}

