package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.RepositoryLanguageMetricsInfo;
import com.optimetric.metrics.application.port.in.GetLanguageMetricsPort;
import com.optimetric.metrics.application.port.out.RepositoryMetricsDataPort;
import com.optimetric.metrics.domain.model.LanguageMetricsResult;
import com.optimetric.metrics.domain.service.LanguageMetricsCalculator;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetLanguageMetricsUseCase implements GetLanguageMetricsPort {

    private final RepositoryMetricsDataPort repositoryMetricsDataPort;

    @Override
    public RepositoryLanguageMetricsInfo getLanguageMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        if (repoFullName == null || repoFullName.isBlank()) {
            throw new IllegalArgumentException("repoFullName cannot be null or blank");
        }
        var input = repositoryMetricsDataPort.getLanguageMetricsInput(login, repoFullName.trim());
        LanguageMetricsResult result = LanguageMetricsCalculator.calculate(input);
        return toInfo(result);
    }

    private static RepositoryLanguageMetricsInfo toInfo(LanguageMetricsResult r) {
        List<RepositoryLanguageMetricsInfo.LanguageStatsInfo> topLanguages = r.topLanguages() == null
                ? List.of()
                : r.topLanguages().stream()
                        .map(s -> new RepositoryLanguageMetricsInfo.LanguageStatsInfo(
                                s.language(), s.bytes(), s.percentage()))
                        .toList();
        return new RepositoryLanguageMetricsInfo(
                r.totalLanguages(), r.primaryLanguage(), r.primaryLanguagePercentage(),
                r.languageDistribution(), r.languageBytes(), topLanguages,
                r.isMultiLanguage(), r.languageDiversityScore()
        );
    }
}
