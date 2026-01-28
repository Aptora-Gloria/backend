package com.optimetric.github.application.dto;

/**
 * Application layer DTO for repository initialization data from GitHub API.
 * This is the contract between application and infrastructure layers.
 */
public record RepositoryInitData(
        Long id,
        String nodeId,
        String name,
        String fullName,
        Owner owner,
        String description,
        String htmlUrl,
        Boolean isPrivate,
        Boolean fork,
        Integer stargazersCount,
        Integer watchersCount,
        Integer forksCount,
        Integer openIssuesCount,
        Integer size,
        String language,
        String defaultBranch,
        String createdAt,    // ISO date-time string from GitHub
        String updatedAt,
        String pushedAt,
        Permissions permissions
) {
    public record Owner(String login, String nodeId, String type) {}
    public record Permissions(Boolean admin, Boolean maintain, Boolean push, Boolean triage, Boolean pull) {}
}

