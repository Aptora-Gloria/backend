package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

/**
 * Application DTO for Pull Request change statistics.
 * Used by application and domain layers only; not exposed in API.
 */
public record ChangeStatsInfo(
        Integer additions,
        Integer deletions,
        Integer changedFiles,
        Boolean available,
        OffsetDateTime syncedAt
) {
    public static ChangeStatsInfo of(Integer additions, Integer deletions, Integer changedFiles, OffsetDateTime syncedAt) {
        boolean available = additions != null && deletions != null && changedFiles != null;
        return new ChangeStatsInfo(additions, deletions, changedFiles, available, syncedAt);
    }

    public static ChangeStatsInfo unavailable() {
        return new ChangeStatsInfo(null, null, null, false, null);
    }
}
