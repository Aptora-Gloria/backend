package com.optimetric.metrics.domain.model;

import java.time.OffsetDateTime;

// Domain için ham inceleme verisi; saf Java.
public record ReviewRaw(
        String state,
        OffsetDateTime submittedAt,
        String reviewerLogin
) {}
