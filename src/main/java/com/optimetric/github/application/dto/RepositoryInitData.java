package com.optimetric.github.application.dto;

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
        String createdAt,    // GitHub'dan gelen ISO tarih-saat
        String updatedAt,
        String pushedAt,
        Permissions permissions
) {
    public record Owner(String login, String nodeId, String type) {}
    public record Permissions(Boolean admin, Boolean maintain, Boolean push, Boolean triage, Boolean pull) {}
}

