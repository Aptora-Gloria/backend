package com.optimetric.github.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * DTO for GitHub Issue Comment API response.
 * Note: Pull Request comments are accessed via the issues endpoint.
 */
public record PullRequestCommentDto(
        @JsonProperty("id") Long githubCommentId,
        @JsonProperty("body") String body,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("user") AuthorInfo author
) {
    public record AuthorInfo(
            @JsonProperty("id") Long githubId,
            @JsonProperty("login") String login
    ) {}
}

