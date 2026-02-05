package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;

public record RepositoryCodeQualityInfo(
        BigDecimal averagePrSize,
        BigDecimal prMergeRate,
        BigDecimal averageReviewsPerPr,
        BigDecimal prsWithoutReviewsPercentage,
        BigDecimal smallPrPercentage,
        BigDecimal mergeCommitPercentage,
        BigDecimal averageCommitMessageLength,
        BigDecimal regularCommitPercentage,
        Long netCodeChange,
        BigDecimal codeGrowthRate,
        BigDecimal averageFilesChangedPerPr,
        BigDecimal overallQualityScore
) {}
