package com.optimetric.metrics.application.dto;

import java.time.OffsetDateTime;

public record RepositoryOverviewInfo(
        String fullName,
        String name,
        String description,
        String htmlUrl,
        String ownerLogin,
        String ownerType,
        String defaultBranch,
        String primaryLanguage,

        Integer stars,
        Integer forks,
        Integer watchers,
        Integer openIssues,

        Boolean isPrivate,
        Boolean isFork,
        Integer size,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime pushedAt,
        OffsetDateTime lastSyncedAt,

        Long repositoryAgeDays,
        Boolean isActive,
        Long daysSinceLastPush
) {}
