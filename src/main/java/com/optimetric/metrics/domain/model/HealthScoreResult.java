package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;

// Sağlık skoru hesaplama sonucu; saf Java.
public record HealthScoreResult(
        BigDecimal activityScore,
        BigDecimal popularityScore,
        BigDecimal codeQualityScore,
        BigDecimal communityScore,
        BigDecimal overallHealthScore,
        String healthStatus,
        HealthBreakdownResult breakdown
) {
    public record HealthBreakdownResult(BigDecimal activityWeight, BigDecimal popularityWeight, BigDecimal qualityWeight, BigDecimal communityWeight) {}
}
