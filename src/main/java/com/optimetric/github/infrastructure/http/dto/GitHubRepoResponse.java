package com.optimetric.github.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GitHub API response DTO for repository data.
 * This is the raw response from GitHub API (snake_case).
 */
public record GitHubRepoResponse(
        Long id,
        @JsonProperty("node_id")
        String nodeId,
        String name,
        @JsonProperty("full_name")
        String fullName,
        Owner owner,
        String description,
        @JsonProperty("html_url")
        String htmlUrl,
        @JsonProperty("private")
        Boolean isPrivate,
        Boolean fork,
        @JsonProperty("stargazers_count")
        Integer stargazersCount,
        @JsonProperty("watchers_count")
        Integer watchersCount,
        @JsonProperty("forks_count")
        Integer forksCount,
        @JsonProperty("open_issues_count")
        Integer openIssuesCount,
        Integer size,
        String language,
        @JsonProperty("default_branch")
        String defaultBranch,
        @JsonProperty("created_at")
        String createdAt,
        @JsonProperty("updated_at")
        String updatedAt,
        @JsonProperty("pushed_at")
        String pushedAt,
        Permissions permissions
) {
    public record Owner(String login, @JsonProperty("node_id") String nodeId, String type) {}
    public record Permissions(Boolean admin, Boolean maintain, Boolean push, Boolean triage, Boolean pull) {}
}
