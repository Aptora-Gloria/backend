package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.HealthScoreInput;
import com.optimetric.metrics.domain.model.HealthScoreResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Sağlık skoru hesaplayıcı; saf Java.
public final class HealthScoreCalculator {

    private HealthScoreCalculator() {}

    public static HealthScoreResult calculate(HealthScoreInput input) {
        BigDecimal activityScore = BigDecimal.ZERO;
        if (input.totalActivityLast30Days() > 0) {
            long a = input.totalActivityLast30Days();
            if (a >= 100) activityScore = BigDecimal.valueOf(100);
            else if (a >= 50) activityScore = BigDecimal.valueOf(80);
            else if (a >= 20) activityScore = BigDecimal.valueOf(60);
            else if (a >= 10) activityScore = BigDecimal.valueOf(40);
            else activityScore = BigDecimal.valueOf(20);
        }

        long popularity = input.stars() + input.forks() + input.watchers();
        BigDecimal popularityScore;
        if (popularity >= 1000) popularityScore = BigDecimal.valueOf(100);
        else if (popularity >= 500) popularityScore = BigDecimal.valueOf(80);
        else if (popularity >= 100) popularityScore = BigDecimal.valueOf(60);
        else if (popularity >= 50) popularityScore = BigDecimal.valueOf(40);
        else if (popularity >= 10) popularityScore = BigDecimal.valueOf(20);
        else popularityScore = BigDecimal.valueOf(10);

        BigDecimal codeQualityScore = input.codeQualityScore() != null ? input.codeQualityScore() : BigDecimal.ZERO;

        long total = input.totalContributors();
        long active = input.activeContributorsLast30Days();
        BigDecimal communityScore = BigDecimal.ZERO;
        if (total >= 10 && active >= 5) communityScore = BigDecimal.valueOf(100);
        else if (total >= 5 && active >= 3) communityScore = BigDecimal.valueOf(80);
        else if (total >= 3 && active >= 2) communityScore = BigDecimal.valueOf(60);
        else if (total >= 2) communityScore = BigDecimal.valueOf(40);
        else if (total >= 1) communityScore = BigDecimal.valueOf(20);

        BigDecimal overall = activityScore.multiply(BigDecimal.valueOf(0.3))
                .add(popularityScore.multiply(BigDecimal.valueOf(0.2)))
                .add(codeQualityScore.multiply(BigDecimal.valueOf(0.3)))
                .add(communityScore.multiply(BigDecimal.valueOf(0.2)))
                .setScale(2, RoundingMode.HALF_UP);

        String healthStatus;
        if (overall.compareTo(BigDecimal.valueOf(80)) >= 0) healthStatus = "EXCELLENT";
        else if (overall.compareTo(BigDecimal.valueOf(60)) >= 0) healthStatus = "GOOD";
        else if (overall.compareTo(BigDecimal.valueOf(40)) >= 0) healthStatus = "FAIR";
        else if (overall.compareTo(BigDecimal.valueOf(20)) >= 0) healthStatus = "POOR";
        else healthStatus = "CRITICAL";

        HealthScoreResult.HealthBreakdownResult breakdown = new HealthScoreResult.HealthBreakdownResult(
                BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.2)
        );

        return new HealthScoreResult(
                activityScore, popularityScore, codeQualityScore, communityScore,
                overall, healthStatus, breakdown
        );
    }
}
