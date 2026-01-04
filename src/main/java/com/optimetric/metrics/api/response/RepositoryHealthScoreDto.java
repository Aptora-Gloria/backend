package com.optimetric.metrics.api.response;

import java.math.BigDecimal;

/**
 * DTO for repository health score.
 * Overall health score calculated from various metrics.
 */
public record RepositoryHealthScoreDto(
        // Individual scores (0-100)
        BigDecimal activityScore,  // Based on recent activity
        BigDecimal popularityScore,  // Based on stars, forks, watchers
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

