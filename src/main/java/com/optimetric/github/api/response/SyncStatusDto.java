package com.optimetric.github.api.response;

import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Data Transfer Object for synchronization status information.
 */
public record SyncStatusDto(
        String repoFullName,
        SyncJobStatus overallStatus,
        List<JobStatusDto> jobs,
        OffsetDateTime lastSyncedAt
) {
    /**
     * Nested record for individual job status information.
     */
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