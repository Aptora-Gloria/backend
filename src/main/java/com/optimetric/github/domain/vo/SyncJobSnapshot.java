package com.optimetric.github.domain.vo;

import java.time.OffsetDateTime;

import com.optimetric.github.domain.enumeration.SyncJobStatus;

public record SyncJobSnapshot(
        SyncJobStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        int attemptCount
) {
    public static SyncJobSnapshot of(SyncJobStatus status, OffsetDateTime startedAt,
                                    OffsetDateTime completedAt, Integer attemptCount) {
        return new SyncJobSnapshot(
                status,
                startedAt,
                completedAt,
                attemptCount != null ? attemptCount : 0
        );
    }
}
