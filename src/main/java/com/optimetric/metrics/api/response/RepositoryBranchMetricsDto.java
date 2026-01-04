package com.optimetric.metrics.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for repository branch metrics.
 */
public record RepositoryBranchMetricsDto(
        Long totalBranches,
        String defaultBranch,
        List<BranchStatsDto> topBranchesByCommits,
        Map<String, Long> commitsByBranch,  // branch -> commit count
        Long branchesWithNoCommits,
        BigDecimal averageCommitsPerBranch
) {
    public record BranchStatsDto(
            String branchName,
            Long commitCount,
            OffsetDateTime lastCommitDate,
            Boolean isDefault
    ) {}
}

