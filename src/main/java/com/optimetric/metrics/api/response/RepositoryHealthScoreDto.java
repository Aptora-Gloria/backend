package com.optimetric.metrics.api.response;

import java.math.BigDecimal;

public record RepositoryHealthScoreDto(
        // Individual scores (0-100)
        BigDecimal activityScore,  // Based on recent activity
        BigDecimal popularityScore,  // Yıldız, fork, watcher'a göre popülerlik
        BigDecimal codeQualityScore,  // Based on PR quality, commit quality
        BigDecimal communityScore,  // Based on contributors, reviews
        
        // Overall health score (0-100)
        BigDecimal overallHealthScore,
        
        // Health status
        String healthStatus,  // EXCELLENT, GOOD, FAIR, POOR, CRITICAL
        
        // Breakdown
        HealthBreakdownDto breakdown
) {
    public record HealthBreakdownDto(
            BigDecimal activityWeight,
            BigDecimal popularityWeight,
            BigDecimal qualityWeight,
            BigDecimal communityWeight
    ) {}
}

