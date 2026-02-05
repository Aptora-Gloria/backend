package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Dil metrikleri hesaplama sonucu; saf Java.
public record LanguageMetricsResult(
        int totalLanguages,
        String primaryLanguage,
        BigDecimal primaryLanguagePercentage,
        Map<String, BigDecimal> languageDistribution,
        Map<String, Long> languageBytes,
        List<LanguageStatsResult> topLanguages,
        boolean isMultiLanguage,
        BigDecimal languageDiversityScore
) {
    public record LanguageStatsResult(String language, long bytes, BigDecimal percentage) {}
}
