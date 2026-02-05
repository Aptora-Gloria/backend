package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import com.optimetric.github.domain.vo.SyncJobSnapshot;

public record SyncJobInfo(
        Long id,
        SyncJobType jobType,
        SyncJobStatus status,
        String repoFullName,
        String cursor,
        Integer attemptCount,
        String errorMessage,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        OffsetDateTime nextRunAt
) {
    public SyncJobSnapshot toSnapshot() {
        return SyncJobSnapshot.of(status(), startedAt(), completedAt(), attemptCount());
    }

    public SyncStatusInfo.JobStatusInfo toJobStatusInfo() {
        return new SyncStatusInfo.JobStatusInfo(
                jobType(),
                status(),
                attemptCount(),
                errorMessage(),
                startedAt(),
                completedAt(),
                nextRunAt()
        );
    }
}
