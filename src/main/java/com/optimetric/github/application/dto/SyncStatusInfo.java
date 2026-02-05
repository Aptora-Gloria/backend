package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;

public record SyncStatusInfo(
        String repoFullName,
        SyncJobStatus overallStatus,
        List<JobStatusInfo> jobs,
        OffsetDateTime lastSyncedAt
) {
    public record JobStatusInfo(
            SyncJobType jobType,
            SyncJobStatus status,
            Integer attemptCount,
            String errorMessage,
            OffsetDateTime startedAt,
            OffsetDateTime completedAt,
            OffsetDateTime nextRunAt
    ) {}
}
