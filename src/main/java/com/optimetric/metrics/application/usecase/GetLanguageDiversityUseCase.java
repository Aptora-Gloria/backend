package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.LanguageDiversityInfo;
import com.optimetric.metrics.application.port.in.GetLanguageDiversityPort;
import com.optimetric.metrics.application.port.out.UserMetricsDataPort;
import com.optimetric.metrics.domain.model.UserLanguageDiversityResult;
import com.optimetric.metrics.domain.service.UserLanguageDiversityCalculator;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetLanguageDiversityUseCase implements GetLanguageDiversityPort {

    private final UserMetricsDataPort userMetricsDataPort;

    @Override
    public LanguageDiversityInfo getLanguageDiversity(String login) throws UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        var input = userMetricsDataPort.getLanguageDiversityInput(login.trim());
        UserLanguageDiversityResult result = UserLanguageDiversityCalculator.calculate(input);
        return toInfo(result);
    }

    private static LanguageDiversityInfo toInfo(UserLanguageDiversityResult r) {
        List<LanguageDiversityInfo.LanguageUsageInfo> topLanguages = r.topLanguages() == null
                ? List.of()
                : r.topLanguages().stream()
                        .map(s -> new LanguageDiversityInfo.LanguageUsageInfo(
                                s.language(), s.repoCount(), s.totalPercentage(), s.totalBytes()))
                        .toList();
        return new LanguageDiversityInfo(
                r.totalLanguages(), r.uniqueLanguagesAcrossRepos(), r.averageLanguagesPerRepo(),
                r.languageUsageCount(), r.languagePercentageDistribution(), topLanguages,
                r.singleLanguageRepos(), r.multiLanguageRepos(), r.multiLanguageReposPercentage()
        );
    }
}
