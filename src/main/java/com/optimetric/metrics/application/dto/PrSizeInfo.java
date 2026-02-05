package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;

public record PrSizeInfo(
        BigDecimal averageAdditions,
        BigDecimal averageDeletions,
        BigDecimal averageChangedFiles,
        BigDecimal averageTotalChanges,
        Long smallPrCount,
        Long mediumPrCount,
        Long largePrCount,
        BigDecimal smallPrPercentage,
        BigDecimal mediumPrPercentage,
        BigDecimal largePrPercentage
) {}
