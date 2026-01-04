package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for Pull Request review statistics.
 */
public record ReviewStatsDto(
        Long totalReviews,
        Long approvedReviews,
        Long changesRequestedReviews,
        Long commentedReviews,
        BigDecimal averageReviewsPerPr,
        BigDecimal approvalRate,  // Percentage (0-100)
        Map<String, Long> reviewsByState,  // state -> count
        Long prsWithoutReviews,
        BigDecimal prsWithReviewsPercentage
) {}

