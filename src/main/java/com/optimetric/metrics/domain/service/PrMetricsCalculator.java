package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.PrMetricsInput;
import com.optimetric.metrics.domain.model.PrMetricsResult;
import com.optimetric.metrics.domain.model.PrRaw;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PrMetricsCalculator {

    private static final int SCALE = 4;
    private static final int TOP_AUTHORS_LIMIT = 10;
    private static final int SMALL_PR_LINES = 100;
    private static final int MEDIUM_PR_MAX_LINES = 500;

    private PrMetricsCalculator() {}

    public static PrMetricsResult calculate(PrMetricsInput input) {
        List<PrRaw> allPrs = input.pullRequests();
        if (allPrs == null || allPrs.isEmpty()) {
            return emptyResult();
        }

        long total = allPrs.size();
        long open = countByState(allPrs, "open");
        long closed = countClosedNotMerged(allPrs);
        long merged = countMerged(allPrs);
        long draft = countDraft(allPrs);

        BigDecimal mergeRate = percentage(merged, total);

        List<PrRaw> prsWithStats = filterWithStats(allPrs);
        BigDecimal avgAdditions = averageAdditions(prsWithStats);
        BigDecimal avgDeletions = averageDeletions(prsWithStats);
        long totalAdditions = prsWithStats.stream().mapToLong(pr -> pr.additions() != null ? pr.additions() : 0).sum();
        long totalDeletions = prsWithStats.stream().mapToLong(pr -> pr.deletions() != null ? pr.deletions() : 0).sum();
        BigDecimal avgChangedFiles = averageChangedFiles(prsWithStats);
        BigDecimal avgTotalChanges = avgAdditions.add(avgDeletions);

        long largestPrSize = maxPrSize(prsWithStats);
        long smallestPrSize = minPrSize(prsWithStats);

        long small = countSmallPrs(prsWithStats);
        long medium = countMediumPrs(prsWithStats);
        long large = countLargePrs(prsWithStats);
        long prsWithStatsCount = prsWithStats.size();
        BigDecimal smallPct = percentage(small, prsWithStatsCount);
        BigDecimal mediumPct = percentage(medium, prsWithStatsCount);
        BigDecimal largePct = percentage(large, prsWithStatsCount);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        OffsetDateTime sevenDaysAgo = now.minusDays(7);

        long prsLast30Days = countCreatedAfter(allPrs, thirtyDaysAgo);
        long prsLast7Days = countCreatedAfter(allPrs, sevenDaysAgo);
        long mergedLast30Days = countMergedAfter(allPrs, thirtyDaysAgo);

        BigDecimal avgMergeTimeHours = averageMergeTimeHours(allPrs);

        Map<String, Long> prsByAuthor = prsByAuthor(allPrs);
        long uniqueAuthors = prsByAuthor.size();
        List<PrMetricsResult.AuthorStatsResult> topAuthors = topAuthorsByMergeRate(allPrs, prsByAuthor);

        return new PrMetricsResult(
                total, open, closed, merged, draft, mergeRate,
                avgAdditions, avgDeletions, totalAdditions, totalDeletions, avgChangedFiles, avgTotalChanges,
                largestPrSize, smallestPrSize,
                small, medium, large, smallPct, mediumPct, largePct,
                prsLast30Days, prsLast7Days, mergedLast30Days, avgMergeTimeHours,
                uniqueAuthors, topAuthors, prsByAuthor
        );
    }

    private static PrMetricsResult emptyResult() {
        return new PrMetricsResult(
                0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO,
                0L, 0L, 0L, 0L, 0L,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                0L, 0L, 0L, BigDecimal.ZERO,
                0L, List.of(), Map.of()
        );
    }

    private static long countByState(List<PrRaw> prs, String state) {
        return prs.stream().filter(pr -> state.equals(pr.state())).count();
    }

    private static long countClosedNotMerged(List<PrRaw> prs) {
        return prs.stream()
                .filter(pr -> "closed".equals(pr.state()) && pr.mergedAt() == null)
                .count();
    }

    private static long countMerged(List<PrRaw> prs) {
        return prs.stream().filter(pr -> pr.mergedAt() != null).count();
    }

    private static long countDraft(List<PrRaw> prs) {
        return prs.stream().filter(pr -> Boolean.TRUE.equals(pr.draft())).count();
    }

    private static BigDecimal percentage(long part, long total) {
        if (total <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(part).divide(BigDecimal.valueOf(total), SCALE, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private static List<PrRaw> filterWithStats(List<PrRaw> prs) {
        return prs.stream()
                .filter(pr -> pr.additions() != null && pr.deletions() != null && pr.changedFiles() != null)
                .toList();
    }

    private static int prSize(PrRaw pr) {
        int a = pr.additions() != null ? pr.additions() : 0;
        int d = pr.deletions() != null ? pr.deletions() : 0;
        return a + d;
    }

    private static BigDecimal averageAdditions(List<PrRaw> prs) {
        if (prs.isEmpty()) return BigDecimal.ZERO;
        double avg = prs.stream().mapToInt(pr -> pr.additions() != null ? pr.additions() : 0).average().orElse(0.0);
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal averageDeletions(List<PrRaw> prs) {
        if (prs.isEmpty()) return BigDecimal.ZERO;
        double avg = prs.stream().mapToInt(pr -> pr.deletions() != null ? pr.deletions() : 0).average().orElse(0.0);
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal averageChangedFiles(List<PrRaw> prs) {
        if (prs.isEmpty()) return BigDecimal.ZERO;
        double avg = prs.stream().mapToInt(pr -> pr.changedFiles() != null ? pr.changedFiles() : 0).average().orElse(0.0);
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    private static long maxPrSize(List<PrRaw> prs) {
        if (prs.isEmpty()) return 0;
        return prs.stream().mapToLong(PrMetricsCalculator::prSize).max().orElse(0);
    }

    private static long minPrSize(List<PrRaw> prs) {
        if (prs.isEmpty()) return 0;
        return prs.stream().mapToLong(PrMetricsCalculator::prSize).min().orElse(0);
    }

    private static long countSmallPrs(List<PrRaw> prs) {
        return prs.stream().filter(pr -> prSize(pr) < SMALL_PR_LINES).count();
    }

    private static long countMediumPrs(List<PrRaw> prs) {
        return prs.stream()
                .filter(pr -> {
                    int size = prSize(pr);
                    return size >= SMALL_PR_LINES && size <= MEDIUM_PR_MAX_LINES;
                })
                .count();
    }

    private static long countLargePrs(List<PrRaw> prs) {
        return prs.stream().filter(pr -> prSize(pr) > MEDIUM_PR_MAX_LINES).count();
    }

    private static long countCreatedAfter(List<PrRaw> prs, OffsetDateTime after) {
        return prs.stream()
                .filter(pr -> pr.createdAt() != null && pr.createdAt().isAfter(after))
                .count();
    }

    private static long countMergedAfter(List<PrRaw> prs, OffsetDateTime after) {
        return prs.stream()
                .filter(pr -> pr.mergedAt() != null && pr.mergedAt().isAfter(after))
                .count();
    }

    private static BigDecimal averageMergeTimeHours(List<PrRaw> prs) {
        List<Duration> mergeTimes = new ArrayList<>();
        for (PrRaw pr : prs) {
            if (pr.mergedAt() != null && pr.createdAt() != null) {
                mergeTimes.add(Duration.between(pr.createdAt(), pr.mergedAt()));
            }
        }
        if (mergeTimes.isEmpty()) return BigDecimal.ZERO;
        double avgHours = mergeTimes.stream().mapToLong(Duration::toHours).average().orElse(0.0);
        return BigDecimal.valueOf(avgHours).setScale(2, RoundingMode.HALF_UP);
    }

    private static Map<String, Long> prsByAuthor(List<PrRaw> prs) {
        return prs.stream()
                .filter(pr -> pr.authorLogin() != null)
                .collect(Collectors.groupingBy(PrRaw::authorLogin, Collectors.counting()));
    }

    private static List<PrMetricsResult.AuthorStatsResult> topAuthorsByMergeRate(
            List<PrRaw> allPrs, Map<String, Long> prsByAuthor) {
        return prsByAuthor.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_AUTHORS_LIMIT)
                .map(e -> {
                    String author = e.getKey();
                    long authorPrCount = e.getValue();
                    long authorMerged = allPrs.stream()
                            .filter(pr -> author.equals(pr.authorLogin()) && pr.mergedAt() != null)
                            .count();
                    BigDecimal authorMergeRate = percentage(authorMerged, authorPrCount);
                    return new PrMetricsResult.AuthorStatsResult(author, authorPrCount, authorMerged, authorMergeRate);
                })
                .toList();
    }
}
