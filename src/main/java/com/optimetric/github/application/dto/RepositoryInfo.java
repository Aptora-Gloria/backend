package com.optimetric.github.application.dto;

import com.optimetric.shared.domain.OwnerType;

import java.time.OffsetDateTime;

/**
 * Application layer DTO for repository information.
 * This is the contract between application and infrastructure layers.
 */
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

