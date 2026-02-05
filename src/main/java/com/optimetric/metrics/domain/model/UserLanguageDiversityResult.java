package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Kullanıcı dil çeşitliliği hesaplama sonucu; saf Java.
public record UserLanguageDiversityResult(
        int totalLanguages,
        int uniqueLanguagesAcrossRepos,
        BigDecimal averageLanguagesPerRepo,
        Map<String, Long> languageUsageCount,
        Map<String, BigDecimal> languagePercentageDistribution,
        List<LanguageUsageResult> topLanguages,
        long singleLanguageRepos,
        long multiLanguageRepos,
        BigDecimal multiLanguageReposPercentage
) {
    public record LanguageUsageResult(String language, long repoCount, BigDecimal totalPercentage, long totalBytes) {}
}
