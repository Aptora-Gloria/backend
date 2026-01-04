package com.optimetric.metrics.api.response;

/**
 * Comprehensive DTO containing all user metrics.
 */
public record UserMetricsDto(
        PrMergeRateDto prMergeRate,
        PrSizeDto prSize,
        ReviewStatsDto reviewStats,
        CommitTimeAnalysisDto commitTimeAnalysis,
        LanguageDiversityDto languageDiversity,
        ActivityTrendDto activityTrend
) {}

