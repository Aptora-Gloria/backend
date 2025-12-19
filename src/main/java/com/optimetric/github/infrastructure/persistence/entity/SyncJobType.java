package com.optimetric.github.infrastructure.persistence.entity;

/**
 * Enumeration for different types of synchronization jobs.
 * PR_LIST - Discover and create/update basic PR records
 * PR_DETAIL - Fetch change statistics for specific PRs
 * PR_REVIEWS - Fetch reviews for specific PRs
 * PR_COMMITS - Fetch commits for specific PRs
 */
public enum SyncJobType {
    PR_LIST,
    PR_DETAIL,
    PR_REVIEWS,
    PR_COMMITS
}