package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.OverviewInput;
import com.optimetric.metrics.domain.model.OverviewResult;

import java.time.Duration;
import java.time.OffsetDateTime;

// Genel bakış hesaplayıcı; yaş, isActive, gün sayısı; saf Java.
public final class OverviewCalculator {

    private OverviewCalculator() {}

    public static OverviewResult calculate(OverviewInput input) {
        OffsetDateTime now = OffsetDateTime.now();
        long ageDays = input.createdAt() != null ? Duration.between(input.createdAt(), now).toDays() : 0;
        boolean isActive = input.pushedAt() != null && input.pushedAt().isAfter(now.minusDays(30));
        long daysSinceLastPush = input.pushedAt() != null ? Duration.between(input.pushedAt(), now).toDays() : 0;

        return new OverviewResult(
                input.fullName(), input.name(), input.description(), input.htmlUrl(),
                input.ownerLogin(), input.ownerType(), input.defaultBranch(), input.primaryLanguage(),
                input.stars(), input.forks(), input.watchers(), input.openIssues(),
                input.isPrivate(), input.isFork(), input.size(),
                input.createdAt(), input.updatedAt(), input.pushedAt(), input.lastSyncedAt(),
                ageDays, isActive, daysSinceLastPush
        );
    }
}
