package com.optimetric.metrics.domain.model;

public record CodeQualityInput(
        PrMetricsResult prMetrics,
        CommitMetricsResult commitMetrics,
        ReviewMetricsResult reviewMetrics
) {}
