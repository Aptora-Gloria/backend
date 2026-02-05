package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.LanguageRaw;
import com.optimetric.metrics.domain.model.UserLanguageDiversityInput;
import com.optimetric.metrics.domain.model.UserLanguageDiversityResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Kullanıcı dil çeşitliliği hesaplayıcı; saf Java.
public final class UserLanguageDiversityCalculator {

    private static final int SCALE = 4;
    private static final int TOP_LANGUAGES_LIMIT = 10;

    private UserLanguageDiversityCalculator() {}

    public static UserLanguageDiversityResult calculate(UserLanguageDiversityInput input) {
        List<UserLanguageDiversityInput.RepoLanguagesRaw> repos = input.repos();
        if (repos == null || repos.isEmpty()) {
            return new UserLanguageDiversityResult(
                    0, 0, BigDecimal.ZERO, Map.of(), Map.of(), List.of(),
                    0L, 0L, BigDecimal.ZERO
            );
        }

        int repoCount = repos.size();
        List<LanguageRaw> allLangs = repos.stream()
                .flatMap(r -> r.languages().stream())
                .toList();
        Map<String, Long> languageBytesTotal = allLangs.stream()
                .collect(Collectors.groupingBy(LanguageRaw::languageName, Collectors.summingLong(LanguageRaw::bytes)));
        int totalLanguages = languageBytesTotal.size();
        int uniqueLanguagesAcrossRepos = totalLanguages;
        long totalBytes = languageBytesTotal.values().stream().mapToLong(Long::longValue).sum();

        BigDecimal avgLanguagesPerRepo = repoCount > 0
                ? BigDecimal.valueOf(repos.stream().mapToInt(r -> r.languages().size()).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Long> languageUsageCount = repos.stream()
                .flatMap(r -> r.languages().stream())
                .map(LanguageRaw::languageName)
                .collect(Collectors.groupingBy(l -> l, Collectors.counting()));

        Map<String, BigDecimal> languagePercentageDistribution = languageBytesTotal.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> totalBytes > 0
                                ? BigDecimal.valueOf(e.getValue()).divide(BigDecimal.valueOf(totalBytes), SCALE, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO
                ));

        List<UserLanguageDiversityResult.LanguageUsageResult> topLanguages = languageBytesTotal.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_LANGUAGES_LIMIT)
                .map(e -> {
                    String lang = e.getKey();
                    long bytes = e.getValue();
                    long repoCountForLang = languageUsageCount.getOrDefault(lang, 0L);
                    BigDecimal pct = totalBytes > 0
                            ? BigDecimal.valueOf(bytes).divide(BigDecimal.valueOf(totalBytes), SCALE, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;
                    return new UserLanguageDiversityResult.LanguageUsageResult(lang, repoCountForLang, pct, bytes);
                })
                .toList();

        long singleLanguageRepos = repos.stream().filter(r -> r.languages().size() == 1).count();
        long multiLanguageRepos = repos.stream().filter(r -> r.languages().size() > 1).count();
        BigDecimal multiLanguagePct = repoCount > 0
                ? BigDecimal.valueOf(multiLanguageRepos).divide(BigDecimal.valueOf(repoCount), SCALE, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new UserLanguageDiversityResult(
                totalLanguages, uniqueLanguagesAcrossRepos, avgLanguagesPerRepo,
                languageUsageCount, languagePercentageDistribution, topLanguages,
                singleLanguageRepos, multiLanguageRepos, multiLanguagePct
        );
    }
}
