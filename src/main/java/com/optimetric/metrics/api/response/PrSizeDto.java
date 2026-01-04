package com.optimetric.metrics.api.response;

import java.math.BigDecimal;

/**
 * DTO for Pull Request size metrics.
 */
public record PrSizeDto(
        BigDecimal averageAdditions,
        BigDecimal averageDeletions,
        BigDecimal averageChangedFiles,
        BigDecimal averageTotalChanges,  // additions + deletions
        Long smallPrCount,  // < 100 lines
        Long mediumPrCount,  // 100-500 lines
        Long largePrCount,  // > 500 lines
        BigDecimal smallPrPercentage,
        BigDecimal mediumPrPercentage,
        BigDecimal largePrPercentage
) {}

