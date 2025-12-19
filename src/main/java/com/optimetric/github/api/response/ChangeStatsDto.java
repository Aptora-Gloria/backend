package com.optimetric.github.api.response;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object for Pull Request change statistics.
 * Includes availability indicator to show if change stats have been fetched.
 */
public record ChangeStatsDto(
        Integer additions,
        Integer deletions,
        Integer changedFiles,
        Boolean available,
        OffsetDateTime syncedAt
) {
    /**
     * Creates a ChangeStatsDto with availability determined by presence of statistics.
     * 
     * @param additions Number of lines added
     * @param deletions Number of lines deleted
     * @param changedFiles Number of files changed
     * @param syncedAt When the change stats were last synced
     * @return ChangeStatsDto with appropriate availability flag
     */
    public static ChangeStatsDto of(Integer additions, Integer deletions, Integer changedFiles, OffsetDateTime syncedAt) {
        boolean available = additions != null && deletions != null && changedFiles != null;
        return new ChangeStatsDto(additions, deletions, changedFiles, available, syncedAt);
    }

    /**
     * Creates a ChangeStatsDto indicating that change stats are not available.
     * 
     * @return ChangeStatsDto with available=false and null values
     */
    public static ChangeStatsDto unavailable() {
        return new ChangeStatsDto(null, null, null, false, null);
    }
}