package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// İnceleme metrikleri hesaplama sonucu; saf Java.
public record ReviewMetricsResult(
        long totalReviews,
        long totalPrCount,
        long approvedReviews,
        long changesRequestedReviews,
        long commentedReviews,
        Map<String, Long> reviewsByState,
        long reviewsLast30Days,
        long reviewsLast7Days,
        BigDecimal averageReviewsPerPr,
        long prsWithoutReviews,
        BigDecimal prsWithReviewsPercentage,
        BigDecimal approvalRate,
        long uniqueReviewers,
        List<ReviewerStatsResult> topReviewers,
        Map<String, Long> reviewsByReviewer,
        Map<String, Long> approvalsByReviewer
) {
    public record ReviewerStatsResult(String reviewerLogin, long reviewCount, long approvalCount, BigDecimal approvalRate) {}
}
