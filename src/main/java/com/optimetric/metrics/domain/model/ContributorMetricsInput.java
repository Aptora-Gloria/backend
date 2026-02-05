package com.optimetric.metrics.domain.model;

import java.util.Map;
import java.util.Set;

// Katkıda bulunan metrikleri girdisi; saf Java.
public record ContributorMetricsInput(
        Map<String, Long> commitsByContributor,
        Map<String, Long> prsByContributor,
        Map<String, Long> reviewsByContributor,
        Set<String> activeContributorsLast30Days,
        Set<String> activeContributorsLast7Days
) {
    public ContributorMetricsInput {
        commitsByContributor = commitsByContributor != null ? Map.copyOf(commitsByContributor) : Map.of();
        prsByContributor = prsByContributor != null ? Map.copyOf(prsByContributor) : Map.of();
        reviewsByContributor = reviewsByContributor != null ? Map.copyOf(reviewsByContributor) : Map.of();
        activeContributorsLast30Days = activeContributorsLast30Days != null ? Set.copyOf(activeContributorsLast30Days) : Set.of();
        activeContributorsLast7Days = activeContributorsLast7Days != null ? Set.copyOf(activeContributorsLast7Days) : Set.of();
    }
}
