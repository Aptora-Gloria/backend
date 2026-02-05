package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.RepositoryCommitMetricsInfo;
import com.optimetric.metrics.application.port.in.GetCommitMetricsPort;
import com.optimetric.metrics.application.port.out.RepositoryMetricsDataPort;
import com.optimetric.metrics.domain.model.CommitMetricsResult;
import com.optimetric.metrics.domain.service.CommitMetricsCalculator;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCommitMetricsUseCase implements GetCommitMetricsPort {

    private final RepositoryMetricsDataPort repositoryMetricsDataPort;

    @Override
    public RepositoryCommitMetricsInfo getCommitMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        if (repoFullName == null || repoFullName.isBlank()) {
            throw new IllegalArgumentException("repoFullName cannot be null or blank");
        }
        var input = repositoryMetricsDataPort.getCommitMetricsInput(login, repoFullName.trim());
        CommitMetricsResult result = CommitMetricsCalculator.calculate(input);
        return toInfo(result);
    }

    private static RepositoryCommitMetricsInfo toInfo(CommitMetricsResult r) {
        List<RepositoryCommitMetricsInfo.AuthorStatsInfo> topAuthors = r.topAuthors() == null
                ? List.of()
                : r.topAuthors().stream()
                        .map(a -> new RepositoryCommitMetricsInfo.AuthorStatsInfo(a.authorLogin(), a.commitCount(), a.mergeCommitCount()))
                        .toList();
        return new RepositoryCommitMetricsInfo(
                r.totalCommits(), r.mergeCommits(), r.regularCommits(), r.mergeCommitPercentage(),
                r.commitsLast30Days(), r.commitsLast7Days(), r.lastCommitDate(), r.mostActiveDay(), r.mostActiveHour(),
                r.commitsByHour(), r.commitsByDayOfWeek(), r.weekdayCommits(), r.weekendCommits(),
                r.weekdayPercentage(), r.weekendPercentage(), r.uniqueAuthors(), topAuthors, r.commitsByAuthor(),
                r.averageMessageLength(), r.longestMessageLength(), r.shortestMessageLength()
        );
    }
}
