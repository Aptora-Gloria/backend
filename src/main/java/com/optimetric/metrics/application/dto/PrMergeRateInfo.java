package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;

public record PrMergeRateInfo(
        Long totalPullRequests,
        Long mergedPullRequests,
        Long openPullRequests,
        Long closedPullRequests,
        BigDecimal mergeRate,
        BigDecimal averageMergeTimeHours
) {}
