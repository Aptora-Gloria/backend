package com.optimetric.github.api.response;

import java.time.OffsetDateTime;

public record ChangeStatsDto(
        Integer additions,
        Integer deletions,
        Integer changedFiles,
        Boolean available,
        OffsetDateTime syncedAt
) {
    public static ChangeStatsDto of(Integer additions, Integer deletions, Integer changedFiles, OffsetDateTime syncedAt) {
        boolean available = additions != null && deletions != null && changedFiles != null;
        return new ChangeStatsDto(additions, deletions, changedFiles, available, syncedAt);
    }

    public static ChangeStatsDto unavailable() {
        return new ChangeStatsDto(null, null, null, false, null);
    }
}