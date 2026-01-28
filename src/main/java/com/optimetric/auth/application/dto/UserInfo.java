package com.optimetric.auth.application.dto;

import com.optimetric.shared.domain.OwnerType;

import java.time.OffsetDateTime;

public record UserInfo(
        Long id,
        Long githubUserId,
        String login,
        String accessToken,
        String name,
        String avatarUrl,
        String htmlUrl,
        OwnerType ownerType,
        OffsetDateTime lastSyncedAt
) {}
