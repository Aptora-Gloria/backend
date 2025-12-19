package com.optimetric.github.infrastructure.persistence.entity;

/**
 * Enumeration for synchronization job status.
 * PENDING - Job is waiting to be executed
 * RUNNING - Job is currently being executed
 * COMPLETED - Job completed successfully
 * FAILED - Job failed with an error
 */
public enum SyncJobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}