package com.optimetric.github.infrastructure.core.mapper;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.optimetric.github.application.dto.SyncJobInfo;
import com.optimetric.github.application.dto.SyncStatusInfo;
import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import com.optimetric.github.domain.service.SyncJobPolicy;
import com.optimetric.github.domain.vo.SyncJobSnapshot;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;

public final class SyncJobMapper {

    private SyncJobMapper() {}

    public static SyncJobStatus determineOverallSyncStatus(SyncJobEntity... jobs) {
        if (jobs == null || jobs.length == 0) {
            return SyncJobStatus.PENDING;
        }
        SyncJobSnapshot[] snapshots = Arrays.stream(jobs)
                .filter(j -> j != null)
                .map(j -> SyncJobSnapshot.of(j.getStatus(), j.getStartedAt(), j.getCompletedAt(), j.getAttemptCount() != null ? j.getAttemptCount() : 0))
                .toArray(SyncJobSnapshot[]::new);
        return SyncJobPolicy.determineOverallSyncStatus(snapshots);
    }

    public static SyncJobStatus determineOverallSyncStatus(SyncJobInfo... jobs) {
        if (jobs == null || jobs.length == 0) {
            return SyncJobStatus.PENDING;
        }
        SyncJobSnapshot[] snapshots = Arrays.stream(jobs)
                .filter(j -> j != null)
                .map(SyncJobInfo::toSnapshot)
                .toArray(SyncJobSnapshot[]::new);
        return SyncJobPolicy.determineOverallSyncStatus(snapshots);
    }

    public static OffsetDateTime getLastSyncTime(SyncJobEntity... jobs) {
        if (jobs == null || jobs.length == 0) {
            return null;
        }
        SyncJobSnapshot[] snapshots = Arrays.stream(jobs)
                .filter(j -> j != null)
                .map(j -> SyncJobSnapshot.of(j.getStatus(), j.getStartedAt(), j.getCompletedAt(), j.getAttemptCount() != null ? j.getAttemptCount() : 0))
                .toArray(SyncJobSnapshot[]::new);
        return SyncJobPolicy.getLastSyncTime(snapshots);
    }

    public static OffsetDateTime getLastSyncTime(SyncJobInfo... jobs) {
        if (jobs == null || jobs.length == 0) {
            return null;
        }
        SyncJobSnapshot[] snapshots = Arrays.stream(jobs)
                .filter(j -> j != null)
                .map(SyncJobInfo::toSnapshot)
                .toArray(SyncJobSnapshot[]::new);
        return SyncJobPolicy.getLastSyncTime(snapshots);
    }

    public static SyncJobInfo toSyncJobInfo(SyncJobEntity entity) {
        if (entity == null) return null;
        return new SyncJobInfo(
                entity.getId(),
                entity.getJobType(),
                entity.getStatus(),
                entity.getRepoFullName(),
                entity.getCursor(),
                entity.getAttemptCount(),
                entity.getErrorMessage(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getNextRunAt()
        );
    }

    // --- Application DTO (Port/In response) ---

    public static SyncStatusInfo.JobStatusInfo toJobStatusInfo(SyncJobEntity entity) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
        return new SyncStatusInfo.JobStatusInfo(
                entity.getJobType(),
                entity.getStatus(),
                entity.getAttemptCount(),
                entity.getErrorMessage(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getNextRunAt()
        );
    }

    public static SyncStatusInfo toSyncStatusInfo(String repoFullName, SyncJobStatus overallStatus,
                                                  List<SyncJobEntity> jobs, OffsetDateTime lastSyncTime) {
        List<SyncStatusInfo.JobStatusInfo> jobInfos = jobs == null ? List.of() : jobs.stream()
                .map(SyncJobMapper::toJobStatusInfo)
                .collect(Collectors.toList());
        return new SyncStatusInfo(repoFullName, overallStatus, jobInfos, lastSyncTime);
    }

    public static SyncStatusInfo toSyncStatusInfoFromJobInfos(String repoFullName, SyncJobStatus overallStatus,
                                                              List<SyncJobInfo> jobs, OffsetDateTime lastSyncTime) {
        List<SyncStatusInfo.JobStatusInfo> jobInfos = jobs == null ? List.of() : jobs.stream()
                .map(SyncJobInfo::toJobStatusInfo)
                .collect(Collectors.toList());
        return new SyncStatusInfo(repoFullName, overallStatus, jobInfos, lastSyncTime);
    }
}