package com.optimetric.github.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record GitHubPullRequestReviewResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("user")
        GitHubUserInfo user,

        @JsonProperty("state")
        String state,

        @JsonProperty("body")
        String body,

        @JsonProperty("submitted_at")
        OffsetDateTime submittedAt,

        @JsonProperty("html_url")
        String htmlUrl
) {
    public record GitHubUserInfo(
            @JsonProperty("id")
            Long id,

            @JsonProperty("login")
            String login
    ) {}
}