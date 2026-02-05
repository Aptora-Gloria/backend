package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record LanguageDiversityDto(
        Integer totalLanguages,
        Integer uniqueLanguagesAcrossRepos,
        BigDecimal averageLanguagesPerRepo,
        Map<String, Long> languageUsageCount,  // language -> repo count
        Map<String, BigDecimal> languagePercentageDistribution,  // language -> percentage
        List<LanguageUsageDto> topLanguages,
        Long singleLanguageRepos,
        Long multiLanguageRepos,
        BigDecimal multiLanguageReposPercentage
) {
    public record LanguageUsageDto(
            String language,
            Long repoCount,
            BigDecimal totalPercentage,
            Long totalBytes
    ) {}
}

