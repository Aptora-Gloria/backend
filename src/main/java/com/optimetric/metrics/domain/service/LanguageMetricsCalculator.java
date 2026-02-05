package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.LanguageMetricsInput;
import com.optimetric.metrics.domain.model.LanguageMetricsResult;
import com.optimetric.metrics.domain.model.LanguageRaw;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Dil metrikleri hesaplayıcı; saf Java, framework yok.
public final class LanguageMetricsCalculator {

    private static final int SCALE = 4;
    private static final int TOP_LANGUAGES_LIMIT = 10;

    private LanguageMetricsCalculator() {}

    public static LanguageMetricsResult calculate(LanguageMetricsInput input) {
        List<LanguageRaw> list = input.languages();
        if (list == null || list.isEmpty()) {
            return new LanguageMetricsResult(
                    0, null, BigDecimal.ZERO, Map.of(), Map.of(), List.of(), false, BigDecimal.ZERO
            );
        }

        Map<String, Long> languageBytes = list.stream()
                .collect(Collectors.groupingBy(LanguageRaw::languageName, Collectors.summingLong(LanguageRaw::bytes)));

        int totalLanguages = languageBytes.size();
        long totalBytes = languageBytes.values().stream().mapToLong(Long::longValue).sum();

        String primaryLanguage = languageBytes.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);

        BigDecimal primaryPct = totalBytes > 0 && primaryLanguage != null
                ? BigDecimal.valueOf(languageBytes.getOrDefault(primaryLanguage, 0L))
                        .divide(BigDecimal.valueOf(totalBytes), SCALE, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        Map<String, BigDecimal> distribution = languageBytes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> totalBytes > 0
                                ? BigDecimal.valueOf(e.getValue()).divide(BigDecimal.valueOf(totalBytes), SCALE, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO
                ));

        List<LanguageMetricsResult.LanguageStatsResult> topLanguages = languageBytes.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed())
                .limit(TOP_LANGUAGES_LIMIT)
                .map(e -> {
                    BigDecimal pct = totalBytes > 0
                            ? BigDecimal.valueOf(e.getValue()).divide(BigDecimal.valueOf(totalBytes), SCALE, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;
                    return new LanguageMetricsResult.LanguageStatsResult(e.getKey(), e.getValue(), pct);
                })
                .toList();

        boolean isMultiLanguage = totalLanguages >= 2;
        BigDecimal diversityScore = totalLanguages > 0
                ? BigDecimal.valueOf(Math.min(totalLanguages * 10, 100))
                : BigDecimal.ZERO;

        return new LanguageMetricsResult(
                totalLanguages, primaryLanguage, primaryPct,
                distribution, languageBytes, topLanguages, isMultiLanguage, diversityScore
        );
    }
}
