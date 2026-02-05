package com.optimetric.metrics.domain.model;

import java.time.OffsetDateTime;
import java.util.List;

// Aktivite trendi girdisi: olay tarihleri listesi; saf Java.
public record ActivityTrendInput(
        List<OffsetDateTime> commitDates,
        List<OffsetDateTime> prDates,
        List<OffsetDateTime> reviewDates
) {
    public ActivityTrendInput {
        commitDates = commitDates != null ? List.copyOf(commitDates) : List.of();
        prDates = prDates != null ? List.copyOf(prDates) : List.of();
        reviewDates = reviewDates != null ? List.copyOf(reviewDates) : List.of();
    }
}
