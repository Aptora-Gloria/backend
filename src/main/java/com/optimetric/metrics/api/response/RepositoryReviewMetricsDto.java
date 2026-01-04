package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for repository review metrics.
 */
public record RepositoryReviewMetricsDto(
        // Statistics
        Long totalReviews,
        Long approvedReviews,
        Long changesRequestedReviews,
        Long commentedReviews,
        Map<String, Long> reviewsByState,  // state -> count
        
        // Activity metrics
        Long reviewsLast30Days,
        Long reviewsLast7Days,
        BigDecimal averageReviewsPerPr,
        Long prsWithoutReviews,
        BigDecimal prsWithReviewsPercentage,
        BigDecimal approvalRate,  // Percentage
        
        // Reviewer metrics
        Long uniqueReviewers,
        List<ReviewerStatsDto> topReviewers,
        Map<String, Long> reviewsByReviewer,  // reviewer -> count
        Map<String, Long> approvalsByReviewer  // reviewer -> approval count
        
        // Timing metrics (optional, can be added later)
        // BigDecimal averageReviewTimeHours  // PR created -> first review
) {
    public record ReviewerStatsDto(
            String reviewerLogin,
            Long reviewCount,
            Long approvalCount,
            BigDecimal approvalRate
    ) {}
}

