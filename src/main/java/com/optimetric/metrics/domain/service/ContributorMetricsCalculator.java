package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.ContributorMetricsInput;
import com.optimetric.metrics.domain.model.ContributorMetricsResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Katkıda bulunan metrikleri hesaplayıcı; saf Java.
public final class ContributorMetricsCalculator {

    private static final int SCALE = 4;
    private static final int TOP_LIMIT = 10;

    private ContributorMetricsCalculator() {}

    public static ContributorMetricsResult calculate(ContributorMetricsInput input) {
        Map<String, Long> commits = input.commitsByContributor();
        Map<String, Long> prs = input.prsByContributor();
        Map<String, Long> reviews = input.reviewsByContributor();

        long totalContributors = commits.keySet().stream().collect(Collectors.toSet()).size()
                + prs.keySet().stream().collect(Collectors.toSet()).size()
                + reviews.keySet().stream().collect(Collectors.toSet()).size();
        var allContributors = new java.util.HashSet<String>();
        allContributors.addAll(commits.keySet());
        allContributors.addAll(prs.keySet());
        allContributors.addAll(reviews.keySet());
        totalContributors = allContributors.size();

        long active30 = input.activeContributorsLast30Days().size();
        long active7 = input.activeContributorsLast7Days().size();

        List<ContributorMetricsResult.ContributorStatsResult> topByCommits = commits.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_LIMIT)
                .map(e -> toStats(e.getKey(), e.getValue(), prs.getOrDefault(e.getKey(), 0L), reviews.getOrDefault(e.getKey(), 0L)))
                .toList();

        List<ContributorMetricsResult.ContributorStatsResult> topByPrs = prs.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_LIMIT)
                .map(e -> toStats(e.getKey(), commits.getOrDefault(e.getKey(), 0L), e.getValue(), reviews.getOrDefault(e.getKey(), 0L)))
                .toList();

        List<ContributorMetricsResult.ContributorStatsResult> topByReviews = reviews.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_LIMIT)
                .map(e -> toStats(e.getKey(), commits.getOrDefault(e.getKey(), 0L), prs.getOrDefault(e.getKey(), 0L), e.getValue()))
                .toList();

        List<Long> allContributions = allContributors.stream()
                .map(c -> commits.getOrDefault(c, 0L) + prs.getOrDefault(c, 0L) + reviews.getOrDefault(c, 0L))
                .sorted(Comparator.reverseOrder())
                .toList();

        long totalSum = allContributions.stream().mapToLong(l -> l).sum();
        long top10Sum = allContributions.stream().limit(10).mapToLong(l -> l).sum();
        BigDecimal top10Pct = totalSum > 0
                ? BigDecimal.valueOf(top10Sum).divide(BigDecimal.valueOf(totalSum), SCALE, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        long singleCommit = commits.values().stream().filter(c -> c == 1).count();
        long regular = commits.values().stream().filter(c -> c >= 5).count();

        return new ContributorMetricsResult(
                totalContributors, active30, active7,
                topByCommits, topByPrs, topByReviews,
                commits, prs, reviews,
                top10Pct, singleCommit, regular
        );
    }

    private static ContributorMetricsResult.ContributorStatsResult toStats(String login, long commits, long prs, long reviews) {
        return new ContributorMetricsResult.ContributorStatsResult(login, commits, prs, reviews, commits + prs + reviews);
    }
}
