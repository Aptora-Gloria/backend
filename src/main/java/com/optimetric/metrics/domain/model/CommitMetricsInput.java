package com.optimetric.metrics.domain.model;

import java.util.List;

public record CommitMetricsInput(List<CommitRaw> commits) {
    public CommitMetricsInput {
        commits = commits != null ? List.copyOf(commits) : List.of();
    }
}
