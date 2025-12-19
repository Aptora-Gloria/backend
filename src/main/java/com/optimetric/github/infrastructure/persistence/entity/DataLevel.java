package com.optimetric.github.infrastructure.persistence.entity;

/**
 * Enumeration indicating the completeness of Pull Request data.
 * BASIC - Only basic PR metadata available
 * FULL - All data including change stats, reviews, and commits
 */
public enum DataLevel {
    BASIC,
    FULL
}