package com.optimetric.metrics.api.response;

import java.math.BigDecimal;

public record RepositoryCodeQualityDto(
        // PR Quality indicators
        BigDecimal averagePrSize,  // Smaller is better
        BigDecimal prMergeRate,  // Higher is better
        BigDecimal averageReviewsPerPr,  // Higher is better
        BigDecimal prsWithoutReviewsPercentage,  // Lower is better
        BigDecimal smallPrPercentage,  // Higher is better (< 100 lines)
        
        // Commit Quality indicators
        BigDecimal mergeCommitPercentage,  // Lower is better
        BigDecimal averageCommitMessageLength,  // Reasonable length
        BigDecimal regularCommitPercentage,  // Higher is better
        
        // Code Change metrics
        Long netCodeChange,  // additions - deletions
        BigDecimal codeGrowthRate,  // Percentage
        BigDecimal averageFilesChangedPerPr,
        
        // Overall Quality Score (0-100)
        BigDecimal overallQualityScore
) {}

