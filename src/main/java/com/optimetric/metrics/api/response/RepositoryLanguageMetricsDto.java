package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RepositoryLanguageMetricsDto(
        Integer totalLanguages,
        String primaryLanguage,
        BigDecimal primaryLanguagePercentage,
        Map<String, BigDecimal> languageDistribution,  // language -> percentage
        Map<String, Long> languageBytes,  // language -> bytes
        List<LanguageStatsDto> topLanguages,
        Boolean isMultiLanguage,  // 2+ languages
        BigDecimal languageDiversityScore  // Calculated score
) {
    public record LanguageStatsDto(
            String language,
            Long bytes,
            BigDecimal percentage
    ) {}
}

