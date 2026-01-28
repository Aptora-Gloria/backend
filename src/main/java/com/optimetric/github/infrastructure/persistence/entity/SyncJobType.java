package com.optimetric.github.infrastructure.persistence.entity;

/**
 * Enumeration for different types of synchronization jobs.
 * PR_LIST - Discover and create/update basic PR records
 * PR_DETAIL - Fetch change statistics for specific PRs
 * PR_REVIEWS - Fetch reviews for specific PRs
 * PR_COMMITS - Fetch commits for specific PRs
 * BRANCH_SYNC - Sync repository branches
 * COMMIT_SYNC - Sync repository commits (for a specific branch and time range)
 * LANGUAGE_SYNC - Sync repository languages
 */
public enum SyncJobType {
    PR_LIST,
    PR_DETAIL,
    PR_REVIEWS,
    PR_COMMITS,
    BRANCH_SYNC,
    COMMIT_SYNC,
    LANGUAGE_SYNC
}