package com.optimetric.metrics.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

// Branch metrikleri hesaplama sonucu; saf Java.
public record BranchMetricsResult(
        long totalBranches,
        String defaultBranch,
        List<BranchStatsResult> topBranchesByCommits,
        Map<String, Long> commitsByBranch,
        long branchesWithNoCommits,
        BigDecimal averageCommitsPerBranch
) {
    public record BranchStatsResult(String branchName, long commitCount, OffsetDateTime lastCommitDate, boolean isDefault) {}
}
