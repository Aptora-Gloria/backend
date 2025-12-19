package com.optimetric.github.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record GitHubPullRequestDetailResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("number")
        Integer number,

        @JsonProperty("state")
        String state,

        @JsonProperty("draft")
        Boolean draft,

        @JsonProperty("title")
        String title,

        @JsonProperty("body")
        String body,

        @JsonProperty("html_url")
        String htmlUrl,

        @JsonProperty("merge_commit_sha")
        String mergeCommitSha,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt,

        @JsonProperty("closed_at")
        OffsetDateTime closedAt,

        @JsonProperty("merged_at")
        OffsetDateTime mergedAt,

        @JsonProperty("user")
        GitHubUserInfo user,

        // Change statistics - only available in detailed response
        @JsonProperty("additions")
        Integer additions,

        @JsonProperty("deletions")
        Integer deletions,

        @JsonProperty("changed_files")
        Integer changedFiles
) {
    public record GitHubUserInfo(
            @JsonProperty("id")
            Long id,

            @JsonProperty("login")
            String login
    ) {}
}