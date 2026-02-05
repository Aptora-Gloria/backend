package com.optimetric.metrics.application.dto;

public record UserMetricsInfo(
        PrMergeRateInfo prMergeRate,
        PrSizeInfo prSize,
        ReviewStatsInfo reviewStats,
        CommitTimeAnalysisInfo commitTimeAnalysis,
        LanguageDiversityInfo languageDiversity,
        ActivityTrendInfo activityTrend
) {}
