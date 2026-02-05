package com.optimetric.github.domain.service;

import java.time.OffsetDateTime;
import java.util.List;

import com.optimetric.github.domain.enumeration.SyncJobStatus;
import com.optimetric.github.domain.vo.SyncJobSnapshot;

public final class SyncJobPolicy {

    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    public static final int DEFAULT_RETRY_DELAY_MINUTES = 5;
    public static final int DEFAULT_STUCK_JOB_THRESHOLD_MINUTES = 30;

    private SyncJobPolicy() {}

    public static boolean canStartSync(List<SyncJobSnapshot> jobsForRepo) {
        if (jobsForRepo == null || jobsForRepo.isEmpty()) {
            return true;
        }
        return jobsForRepo.stream()
                .noneMatch(j -> j.status() == SyncJobStatus.RUNNING || j.status() == SyncJobStatus.PENDING);
    }

    public static boolean isJobStuck(SyncJobSnapshot job, OffsetDateTime now, int stuckThresholdMinutes) {
        if (job == null || job.status() != SyncJobStatus.RUNNING) {
            return false;
        }
        OffsetDateTime started = job.startedAt();
        if (started == null) {
            return false;
        }
        OffsetDateTime threshold = now.minusMinutes(stuckThresholdMinutes);
        return started.isBefore(threshold) || started.isEqual(threshold);
    }

    public static boolean shouldScheduleRetry(SyncJobSnapshot job, int maxAttempts) {
        if (job == null || job.status() != SyncJobStatus.FAILED) {
            return false;
        }
        return job.attemptCount() < maxAttempts;
    }

    public static int retryDelayMinutes(SyncJobSnapshot job, int baseDelayMinutes) {
        if (job == null) {
            return baseDelayMinutes;
        }
        return baseDelayMinutes * Math.max(1, job.attemptCount());
    }

    public static boolean shouldCleanup(SyncJobSnapshot job, OffsetDateTime cutoffTime) {
        if (job == null || cutoffTime == null) {
            return false;
        }
        if (job.status() != SyncJobStatus.COMPLETED && job.status() != SyncJobStatus.FAILED) {
            return false;
        }
        OffsetDateTime completed = job.completedAt();
        if (completed == null) {
            return false;
        }
        return completed.isBefore(cutoffTime) || completed.isEqual(cutoffTime);
    }

    public static SyncJobStatus determineOverallSyncStatus(SyncJobSnapshot... jobs) {
        boolean hasRunning = false;
        boolean hasFailed = false;
        boolean hasCompleted = false;

        if (jobs != null) {
            for (SyncJobSnapshot job : jobs) {
                if (job == null) continue;
                SyncJobStatus status = job.status();
                if (status == null) continue;
                switch (status) {
                    case RUNNING -> hasRunning = true;
                    case FAILED -> hasFailed = true;
                    case COMPLETED -> hasCompleted = true;
                    default -> {}
                }
            }
        }

        if (hasRunning) return SyncJobStatus.RUNNING;
        if (hasFailed) return SyncJobStatus.FAILED;
        if (hasCompleted) return SyncJobStatus.COMPLETED;
        return SyncJobStatus.PENDING;
    }

    public static OffsetDateTime getLastSyncTime(SyncJobSnapshot... jobs) {
        OffsetDateTime lastTime = null;
        if (jobs == null) return lastTime;
        for (SyncJobSnapshot job : jobs) {
            if (job == null || job.completedAt() == null) continue;
            OffsetDateTime completed = job.completedAt();
            if (lastTime == null || completed.isAfter(lastTime)) {
                lastTime = completed;
            }
        }
        return lastTime;
    }
}
