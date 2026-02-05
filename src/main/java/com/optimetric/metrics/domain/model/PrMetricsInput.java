package com.optimetric.metrics.domain.model;

import java.util.List;

public record PrMetricsInput(List<PrRaw> pullRequests) {
    public PrMetricsInput {
        pullRequests = pullRequests != null ? List.copyOf(pullRequests) : List.of();
    }
}
