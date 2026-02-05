package com.optimetric.metrics.domain.model;

import java.util.List;

// Branch metrikleri girdisi; saf Java.
public record BranchMetricsInput(String defaultBranch, List<BranchRaw> branches) {
    public BranchMetricsInput {
        branches = branches != null ? List.copyOf(branches) : List.of();
    }
}
