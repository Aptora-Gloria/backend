package com.optimetric.metrics.domain.model;

import java.util.List;

// Dil metrikleri girdisi; saf Java.
public record LanguageMetricsInput(List<LanguageRaw> languages) {
    public LanguageMetricsInput {
        languages = languages != null ? List.copyOf(languages) : List.of();
    }
}
