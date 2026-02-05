package com.optimetric.metrics.domain.model;

import java.time.OffsetDateTime;

public record CommitRaw(
        String authorLogin,
        OffsetDateTime committedAt,
        Boolean mergeCommit,
        int messageLength
) {}
