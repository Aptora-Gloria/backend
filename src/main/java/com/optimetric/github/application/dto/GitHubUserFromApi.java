package com.optimetric.github.application.dto;

public record GitHubUserFromApi(
        Long githubUserId,
        String login,
        String name,
        String avatarUrl,
        String htmlUrl,
        String type
) {}
