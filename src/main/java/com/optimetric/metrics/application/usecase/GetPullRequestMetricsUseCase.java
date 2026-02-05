package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.RepositoryPrMetricsInfo;
import com.optimetric.metrics.application.port.in.GetPullRequestMetricsPort;
import com.optimetric.metrics.application.port.out.RepositoryMetricsDataPort;
import com.optimetric.metrics.domain.model.PrMetricsResult;
import com.optimetric.metrics.domain.service.PrMetricsCalculator;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPullRequestMetricsUseCase implements GetPullRequestMetricsPort {

    private final RepositoryMetricsDataPort repositoryMetricsDataPort;

    @Override
    public RepositoryPrMetricsInfo getPullRequestMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        if (repoFullName == null || repoFullName.isBlank()) {
            throw new IllegalArgumentException("repoFullName cannot be null or blank");
        }
        var input = repositoryMetricsDataPort.getPrMetricsInput(login, repoFullName.trim());
        PrMetricsResult result = PrMetricsCalculator.calculate(input);
        return toInfo(result);
    }

    private static RepositoryPrMetricsInfo toInfo(PrMetricsResult r) {
        List<RepositoryPrMetricsInfo.AuthorStatsInfo> topAuthors = r.topAuthors() == null
                ? List.of()
                : r.topAuthors().stream()
                        .map(a -> new RepositoryPrMetricsInfo.AuthorStatsInfo(
                                a.authorLogin(), a.prCount(), a.mergedPrCount(), a.mergeRate()))
                        .toList();
        return new RepositoryPrMetricsInfo(
                r.totalPullRequests(), r.openPullRequests(), r.closedPullRequests(),
                r.mergedPullRequests(), r.draftPullRequests(), r.mergeRate(),
                r.averageAdditions(), r.averageDeletions(), r.averageChangedFiles(),
                r.averageTotalChanges(), r.largestPrSize(), r.smallestPrSize(),
                r.smallPrCount(), r.mediumPrCount(), r.largePrCount(),
                r.smallPrPercentage(), r.mediumPrPercentage(), r.largePrPercentage(),
                r.prsLast30Days(), r.prsLast7Days(), r.mergedLast30Days(),
                r.averageMergeTimeHours(), r.uniqueAuthors(), topAuthors, r.prsByAuthor()
        );
    }
}
