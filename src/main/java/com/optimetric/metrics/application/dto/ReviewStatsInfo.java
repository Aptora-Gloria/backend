package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ReviewStatsInfo(
        Long totalReviews,
        Long approvedReviews,
        Long changesRequestedReviews,
        Long commentedReviews,
        BigDecimal averageReviewsPerPr,
        BigDecimal approvalRate,
        Map<String, Long> reviewsByState,
        Long prsWithoutReviews,
        BigDecimal prsWithReviewsPercentage
) {}
