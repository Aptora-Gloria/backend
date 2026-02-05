package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;

public record RepositoryHealthScoreInfo(
        BigDecimal activityScore,
        BigDecimal popularityScore,
        BigDecimal codeQualityScore,
        BigDecimal communityScore,
        BigDecimal overallHealthScore,
        String healthStatus,
        HealthBreakdownInfo breakdown
) {
    public record HealthBreakdownInfo(
            BigDecimal activityWeight,
            BigDecimal popularityWeight,
            BigDecimal qualityWeight,
            BigDecimal communityWeight
    ) {}
}
