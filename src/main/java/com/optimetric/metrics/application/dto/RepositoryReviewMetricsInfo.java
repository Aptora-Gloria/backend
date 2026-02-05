package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RepositoryReviewMetricsInfo(
        Long totalReviews,
        Long approvedReviews,
        Long changesRequestedReviews,
        Long commentedReviews,
        Map<String, Long> reviewsByState,
        Long reviewsLast30Days,
        Long reviewsLast7Days,
        BigDecimal averageReviewsPerPr,
        Long prsWithoutReviews,
        BigDecimal prsWithReviewsPercentage,
        BigDecimal approvalRate,
        Long uniqueReviewers,
        List<ReviewerStatsInfo> topReviewers,
        Map<String, Long> reviewsByReviewer,
        Map<String, Long> approvalsByReviewer
) {
    public record ReviewerStatsInfo(
            String reviewerLogin,
            Long reviewCount,
            Long approvalCount,
            BigDecimal approvalRate
    ) {}
}
