package com.optimetric.metrics.api.response;

public record UserMetricsDto(
        PrMergeRateDto prMergeRate,
        PrSizeDto prSize,
        ReviewStatsDto reviewStats,
        CommitTimeAnalysisDto commitTimeAnalysis,
        LanguageDiversityDto languageDiversity,
        ActivityTrendDto activityTrend
) {}

