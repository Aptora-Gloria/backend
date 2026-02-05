package com.optimetric.metrics.api.response;

import java.math.BigDecimal;

public record PrMergeRateDto(
        Long totalPullRequests,
        Long mergedPullRequests,
        Long openPullRequests,
        Long closedPullRequests,
        BigDecimal mergeRate,  // Percentage (0-100)
        BigDecimal averageMergeTimeHours  // Oluşturmadan merge'e ortalama süre (saat)
) {}

