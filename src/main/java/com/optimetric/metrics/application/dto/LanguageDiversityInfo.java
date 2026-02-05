package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record LanguageDiversityInfo(
        Integer totalLanguages,
        Integer uniqueLanguagesAcrossRepos,
        BigDecimal averageLanguagesPerRepo,
        Map<String, Long> languageUsageCount,
        Map<String, BigDecimal> languagePercentageDistribution,
        List<LanguageUsageInfo> topLanguages,
        Long singleLanguageRepos,
        Long multiLanguageRepos,
        BigDecimal multiLanguageReposPercentage
) {
    public record LanguageUsageInfo(
            String language,
            Long repoCount,
            BigDecimal totalPercentage,
            Long totalBytes
    ) {}
}
