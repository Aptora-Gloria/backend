package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RepositoryLanguageMetricsInfo(
        Integer totalLanguages,
        String primaryLanguage,
        BigDecimal primaryLanguagePercentage,
        Map<String, BigDecimal> languageDistribution,
        Map<String, Long> languageBytes,
        List<LanguageStatsInfo> topLanguages,
        Boolean isMultiLanguage,
        BigDecimal languageDiversityScore
) {
    public record LanguageStatsInfo(
            String language,
            Long bytes,
            BigDecimal percentage
    ) {}
}
