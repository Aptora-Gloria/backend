package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;

// Kod kalitesi hesaplama sonucu; saf Java.
public record CodeQualityResult(
        BigDecimal averagePrSize,
        BigDecimal prMergeRate,
        BigDecimal averageReviewsPerPr,
        BigDecimal prsWithoutReviewsPercentage,
        BigDecimal smallPrPercentage,
        BigDecimal mergeCommitPercentage,
        BigDecimal averageCommitMessageLength,
        BigDecimal regularCommitPercentage,
        long netCodeChange,
        BigDecimal codeGrowthRate,
        BigDecimal averageFilesChangedPerPr,
        BigDecimal overallQualityScore
) {}
