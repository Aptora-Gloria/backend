package com.optimetric.github.application.dto;

import java.time.OffsetDateTime;

public record BranchInfo(
        String name,
        Long commitCount,
        OffsetDateTime lastCommitDate
) {}
