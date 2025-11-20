package com.optimetric.auth.api.dto;

public record GitHubUserDto(
        Long id,
        Long githubUserId,
        String login,
        String accessToken,
        String name,
        String avatar_url,


        String htmlUrl,

        String ownerType,
        String lastSyncedAt
) {}
