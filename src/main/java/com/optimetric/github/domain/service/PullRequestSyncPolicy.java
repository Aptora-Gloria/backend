package com.optimetric.github.domain.service;

import java.time.OffsetDateTime;

public final class PullRequestSyncPolicy {

    private PullRequestSyncPolicy() {}

    public static boolean needsSync(OffsetDateTime lastSyncedAt, OffsetDateTime updatedAt) {
        if (lastSyncedAt == null) {
            return true;
        }
        if (updatedAt == null) {
            return false;
        }
        return updatedAt.isAfter(lastSyncedAt);
    }
}
