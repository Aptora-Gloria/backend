package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;

// Sağlık skoru girdisi; saf Java.
public record HealthScoreInput(
        long totalActivityLast30Days,
        int stars,
        int forks,
        int watchers,
        BigDecimal codeQualityScore,
        long totalContributors,
        long activeContributorsLast30Days
) {}
