package com.optimetric.metrics.api.response;

import java.time.OffsetDateTime;

public record RepositoryOverviewDto(
        String fullName,
        String name,
        String description,
        String htmlUrl,
        String ownerLogin,
        String ownerType,
        String defaultBranch,
        String primaryLanguage,
        
        // Popularity metrics
        Integer stars,
        Integer forks,
        Integer watchers,
        Integer openIssues,
        
        // Repository properties
        Boolean isPrivate,
        Boolean isFork,
        Integer size,  // KB
        
        // Timestamps
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime pushedAt,
        OffsetDateTime lastSyncedAt,
        
        // Calculated metrics
        Long repositoryAgeDays,  // Days since creation
        Boolean isActive,  // Has activity in last 30 days
        Long daysSinceLastPush  // Days since last push
) {}

