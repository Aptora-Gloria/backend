package com.optimetric.github.api.response;

import java.time.OffsetDateTime;
import java.util.List;

import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;

public record SyncStatusDto(
        String repoFullName,
        SyncJobStatus overallStatus,
        List<JobStatusDto> jobs,
        OffsetDateTime lastSyncedAt
) {
    public record JobStatusDto(
            SyncJobType jobType,
            SyncJobStatus status,
            Integer attemptCount,
            String errorMessage,
            OffsetDateTime startedAt,
            OffsetDateTime completedAt,
            OffsetDateTime nextRunAt
    ) {}
}