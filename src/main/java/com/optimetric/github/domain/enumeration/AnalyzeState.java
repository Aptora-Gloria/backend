package com.optimetric.github.domain.enumeration;

/**
 * State returned by the analyze endpoint.
 * Indicates the result of the analysis and job enqueueing operation.
 */
public enum AnalyzeState {
    /**
     * Sync jobs were started/enqueued for missing data components.
     */
    SYNC_STARTED,
    
    /**
     * Repository is already ready (all data components available).
     */
    ALREADY_READY,
    
    /**
     * Sync is already running (jobs are PENDING or RUNNING).
     */
    SYNC_ALREADY_RUNNING
}
