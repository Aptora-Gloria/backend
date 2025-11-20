package com.optimetric.github.api.response;


import com.optimetric.auth.infrastructure.persistence.entity.OwnerType;

import java.time.OffsetDateTime;

public record RepoDto(
        Long id,
        Long githubRepoId,
        String name,
        String fullName,
        String description,
        String htmlUrl,
        Boolean isPrivate,
        Boolean isFork,
        Integer stargazersCount,
        Integer watchersCount,
        Integer forksCount,
        Integer openIssuesCount,
        Integer size,
        String language,
        String defaultBranch,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime pushedAt,
        String ownerLogin,
        OwnerType ownerType,
        OffsetDateTime lastSyncedAt
) {}


