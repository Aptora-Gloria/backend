package com.optimetric.github.domain.enumeration;

/**
 * Enumeration for job types that can be enqueued by the analyze endpoint.
 * Used in response DTO to indicate which jobs were enqueued.
 */
public enum EnqueuedJobType {
    BRANCH_SYNC,
    PR_LIST,
    LANGUAGE_SYNC,
    COMMIT_SYNC
}
