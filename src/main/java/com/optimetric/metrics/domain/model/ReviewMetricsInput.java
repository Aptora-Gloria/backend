package com.optimetric.metrics.domain.model;

import java.util.List;

// İnceleme metrikleri girdisi; saf Java.
public record ReviewMetricsInput(
        List<ReviewRaw> reviews,
        long totalPrCount,
        long prsWithoutReviewsCount
) {
    public ReviewMetricsInput {
        reviews = reviews != null ? List.copyOf(reviews) : List.of();
    }
}
