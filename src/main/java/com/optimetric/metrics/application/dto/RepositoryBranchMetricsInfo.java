package com.optimetric.metrics.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record RepositoryBranchMetricsInfo(
        Long totalBranches,
        String defaultBranch,
        List<BranchStatsInfo> topBranchesByCommits,
        Map<String, Long> commitsByBranch,
        Long branchesWithNoCommits,
        BigDecimal averageCommitsPerBranch
) {
    public record BranchStatsInfo(
            String branchName,
            Long commitCount,
            OffsetDateTime lastCommitDate,
            Boolean isDefault
    ) {}
}
