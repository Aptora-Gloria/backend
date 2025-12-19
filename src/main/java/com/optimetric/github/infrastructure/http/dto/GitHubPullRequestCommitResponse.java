package com.optimetric.github.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record GitHubPullRequestCommitResponse(
        @JsonProperty("sha")
        String sha,

        @JsonProperty("commit")
        GitHubCommitInfo commit,

        @JsonProperty("html_url")
        String htmlUrl,

        @JsonProperty("author")
        GitHubUserInfo author
) {
    public record GitHubCommitInfo(
            @JsonProperty("message")
            String message,

            @JsonProperty("author")
            GitHubCommitAuthor author
    ) {}

    public record GitHubCommitAuthor(
            @JsonProperty("name")
            String name,

            @JsonProperty("email")
            String email,

            @JsonProperty("date")
            OffsetDateTime date
    ) {}

    public record GitHubUserInfo(
            @JsonProperty("login")
            String login
    ) {}
}