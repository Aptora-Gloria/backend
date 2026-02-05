package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.RepositoryBranchMetricsInfo;
import com.optimetric.metrics.application.port.in.GetBranchMetricsPort;
import com.optimetric.metrics.application.port.out.RepositoryMetricsDataPort;
import com.optimetric.metrics.domain.model.BranchMetricsResult;
import com.optimetric.metrics.domain.service.BranchMetricsCalculator;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetBranchMetricsUseCase implements GetBranchMetricsPort {

    private final RepositoryMetricsDataPort repositoryMetricsDataPort;

    @Override
    public RepositoryBranchMetricsInfo getBranchMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        if (repoFullName == null || repoFullName.isBlank()) {
            throw new IllegalArgumentException("repoFullName cannot be null or blank");
        }
        var input = repositoryMetricsDataPort.getBranchMetricsInput(login, repoFullName.trim());
        BranchMetricsResult result = BranchMetricsCalculator.calculate(input);
        return toInfo(result);
    }

    private static RepositoryBranchMetricsInfo toInfo(BranchMetricsResult r) {
        List<RepositoryBranchMetricsInfo.BranchStatsInfo> topBranches = r.topBranchesByCommits() == null
                ? List.of()
                : r.topBranchesByCommits().stream()
                        .map(s -> new RepositoryBranchMetricsInfo.BranchStatsInfo(
                                s.branchName(), s.commitCount(), s.lastCommitDate(), s.isDefault()))
                        .toList();
        return new RepositoryBranchMetricsInfo(
                r.totalBranches(), r.defaultBranch(), topBranches,
                r.commitsByBranch(), r.branchesWithNoCommits(), r.averageCommitsPerBranch()
        );
    }
}
