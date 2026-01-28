package com.optimetric.github.domain.enumeration;

/**
 * Missing data components for repository readiness.
 * Used to indicate which specific data components are not yet available.
 * 
 * IMPORTANT: The order of enum values is significant and must match the order
 * specified in requirements: BRANCHES, PRS, PR_DETAILS, REVIEWS, COMMITS, LANGUAGES
 */
public enum MissingComponent {
    BRANCHES,
    PRS,
    PR_DETAILS,
    REVIEWS,
    COMMITS,
    LANGUAGES
}
