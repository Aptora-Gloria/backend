package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

import com.optimetric.shared.domain.OwnerType;

public record RepositoryInfo(
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

