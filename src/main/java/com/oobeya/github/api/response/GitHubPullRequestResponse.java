package com.oobeya.github.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * GitHub API response DTO for pull requests.
 * Maps directly from GitHub API JSON response.
 */
public record GitHubPullRequestResponse(
        Long id,
        Integer number,
        String state,
        String title,
        String body,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("api_url") String url,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt,
        @JsonProperty("closed_at") String closedAt,
        @JsonProperty("merged_at") String mergedAt,
        Boolean merged,
        Boolean draft,
        User user,
        Head head,
        Base base
) {
    public record User(
            String login,
            Long id
    ) {}

    public record Head(
            String ref,
            String sha
    ) {}

    public record Base(
            String ref,
            String sha
    ) {}
}



