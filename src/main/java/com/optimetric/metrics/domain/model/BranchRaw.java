package com.optimetric.metrics.domain.model;

import java.time.OffsetDateTime;

// Ham branch verisi (adapter'dan gelen commit sayısı ile).
public record BranchRaw(
        String branchName,
        long commitCount,
        OffsetDateTime lastCommitDate,
        boolean isDefault
) {}
