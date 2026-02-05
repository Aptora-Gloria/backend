package com.optimetric.metrics.domain.model;

import java.time.OffsetDateTime;

public record PrRaw(
        String authorLogin,
        String state,
        OffsetDateTime mergedAt,
        OffsetDateTime createdAt,
        Integer additions,
        Integer deletions,
        Integer changedFiles,
        Boolean draft
) {}
