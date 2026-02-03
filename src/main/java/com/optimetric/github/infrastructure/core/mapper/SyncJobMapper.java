package com.optimetric.github.infrastructure.core.mapper;

import com.optimetric.github.api.response.SyncStatusDto;
import com.optimetric.github.application.dto.SyncStatusInfo;
import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.enumeration.SyncJobType;
import com.optimetric.github.infrastructure.persistence.entity.SyncJobEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for SyncJob entities to DTOs.
 * Handles conversion between sync job entities and API response DTOs.
 */
public final class SyncJobMapper {

    private SyncJobMapper() {}

    /**
     * Maps SyncJobEntity to SyncStatusDto.JobStatusDto for API response.
     * 
     * @param entity The sync job entity
     * @return JobStatusDto with job information
     */
    public static SyncStatusDto.JobStatusDto toJobStatusDto(SyncJobEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        return new SyncStatusDto.JobStatusDto(
                entity.getJobType() != null ? SyncJobType.valueOf(entity.getJobType().name()) : null,
                entity.getStatus() != null ? SyncJobStatus.valueOf(entity.getStatus().name()) : null,
                entity.getAttemptCount(),
                entity.getErrorMessage(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getNextRunAt()
        );
    }

    /**
     * Maps list of SyncJobEntity to list of JobStatusDto.
     * 
     * @param entities List of sync job entities
     * @return List of JobStatusDto
     */
    public static List<SyncStatusDto.JobStatusDto> toJobStatusDtos(List<SyncJobEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(SyncJobMapper::toJobStatusDto)
                .collect(Collectors.toList());
    }

    /**
     * Creates a complete SyncStatusDto from repository information and job entities.
     * 
     * @param repoFullName Repository full name
     * @param overallStatus Overall synchronization status
     * @param jobs List of sync job entities
     * @param lastSyncTime Last synchronization time
     * @return Complete SyncStatusDto
     */
    public static SyncStatusDto toSyncStatusDto(String repoFullName, 
                                               SyncJobStatus overallStatus,
                                               List<SyncJobEntity> jobs, 
                                               OffsetDateTime lastSyncTime) {
        List<SyncStatusDto.JobStatusDto> jobDtos = toJobStatusDtos(jobs);
        
        return new SyncStatusDto(
                repoFullName,
                overallStatus,
                jobDtos,
                lastSyncTime
        );
    }

    /**
     * Determines the overall sync status based on individual job statuses.
     * 
     * @param jobs Array of sync job entities
     * @return Overall sync status
     */
    public static SyncJobStatus determineOverallSyncStatus(SyncJobEntity... jobs) {
        boolean hasRunning = false;
        boolean hasFailed = false;
        boolean hasCompleted = false;
        
        for (SyncJobEntity job : jobs) {
            if (job == null) continue;
            
            var status = job.getStatus();
            if (status == null) continue;
            switch (status.name()) {
                case "RUNNING" -> hasRunning = true;
                case "FAILED" -> hasFailed = true;
                case "COMPLETED" -> hasCompleted = true;
            }
        }
        
        if (hasRunning) {
            return SyncJobStatus.RUNNING;
        } else if (hasFailed) {
            return SyncJobStatus.FAILED;
        } else if (hasCompleted) {
            return SyncJobStatus.COMPLETED;
        } else {
            return SyncJobStatus.PENDING;
        }
    }

    /**
     * Gets the most recent completion time from all jobs.
     * 
     * @param jobs Array of sync job entities
     * @return Most recent completion time or null if no jobs completed
     */
    public static OffsetDateTime getLastSyncTime(SyncJobEntity... jobs) {
        OffsetDateTime lastTime = null;
        
        for (SyncJobEntity job : jobs) {
            if (job == null || job.getCompletedAt() == null) continue;
            
            if (lastTime == null || job.getCompletedAt().isAfter(lastTime)) {
                lastTime = job.getCompletedAt();
            }
        }
        
        return lastTime;
    }

    // --- Application DTO (Port/In response) ---

    public static SyncStatusInfo.JobStatusInfo toJobStatusInfo(SyncJobEntity entity) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
        SyncJobType jobType = entity.getJobType() != null ? SyncJobType.valueOf(entity.getJobType().name()) : null;
        SyncJobStatus status = entity.getStatus() != null ? SyncJobStatus.valueOf(entity.getStatus().name()) : null;
        return new SyncStatusInfo.JobStatusInfo(
                jobType,
                status,
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
}