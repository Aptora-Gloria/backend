package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.BranchMetricsInput;
import com.optimetric.metrics.domain.model.BranchMetricsResult;
import com.optimetric.metrics.domain.model.BranchRaw;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Branch metrikleri hesaplayıcı; saf Java.
public final class BranchMetricsCalculator {

    private static final int TOP_BRANCHES_LIMIT = 10;

    private BranchMetricsCalculator() {}

    public static BranchMetricsResult calculate(BranchMetricsInput input) {
        List<BranchRaw> branches = input.branches();
        String defaultBranch = input.defaultBranch();
        if (branches == null || branches.isEmpty()) {
            return new BranchMetricsResult(0L, defaultBranch, List.of(), Map.of(), 0L, BigDecimal.ZERO);
        }

        Map<String, Long> commitsByBranch = branches.stream()
                .collect(Collectors.toMap(BranchRaw::branchName, BranchRaw::commitCount));

        List<BranchMetricsResult.BranchStatsResult> top = branches.stream()
                .sorted(Comparator.comparingLong(BranchRaw::commitCount).reversed())
                .limit(TOP_BRANCHES_LIMIT)
                .map(b -> new BranchMetricsResult.BranchStatsResult(
                        b.branchName(), b.commitCount(), b.lastCommitDate(), b.isDefault()))
                .toList();

        long branchesWithNoCommits = branches.stream().filter(b -> b.commitCount() == 0).count();
        long total = branches.size();
        long sum = branches.stream().mapToLong(BranchRaw::commitCount).sum();
        BigDecimal avg = total > 0
                ? BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new BranchMetricsResult(
                total, defaultBranch, top, commitsByBranch, branchesWithNoCommits, avg
        );
    }
}
