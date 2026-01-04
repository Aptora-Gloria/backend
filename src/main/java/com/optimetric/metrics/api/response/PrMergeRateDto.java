package com.optimetric.metrics.api.response;

import java.math.BigDecimal;

/**
 * DTO for Pull Request merge rate metrics.
 */
public record PrMergeRateDto(
        Long totalPullRequests,
        Long mergedPullRequests,
        Long openPullRequests,
        Long closedPullRequests,
        BigDecimal mergeRate,  // Percentage (0-100)
        BigDecimal averageMergeTimeHours  // Average time from creation to merge
) {}

