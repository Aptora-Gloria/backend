package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.CommitMetricsInput;
import com.optimetric.metrics.domain.model.CommitMetricsResult;
import com.optimetric.metrics.domain.model.CommitRaw;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CommitMetricsCalculator {

    private static final int SCALE = 4;
    private static final int TOP_AUTHORS_LIMIT = 10;

    private CommitMetricsCalculator() {}

    public static CommitMetricsResult calculate(CommitMetricsInput input) {
        List<CommitRaw> allCommits = input.commits();
        if (allCommits == null || allCommits.isEmpty()) {
            return emptyResult();
        }

        long total = allCommits.size();
        long mergeCommits = allCommits.stream().filter(c -> Boolean.TRUE.equals(c.mergeCommit())).count();
        long regularCommits = total - mergeCommits;
        BigDecimal mergeCommitPercentage = percentage(mergeCommits, total);

        List<CommitRaw> withDate = allCommits.stream().filter(c -> c.committedAt() != null).toList();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        OffsetDateTime sevenDaysAgo = now.minusDays(7);

        long commitsLast30Days = withDate.stream().filter(c -> c.committedAt().isAfter(thirtyDaysAgo)).count();
        long commitsLast7Days = withDate.stream().filter(c -> c.committedAt().isAfter(sevenDaysAgo)).count();

        OffsetDateTime lastCommitDate = withDate.stream()
                .map(CommitRaw::committedAt)
                .max(OffsetDateTime::compareTo)
                .orElse(null);

        Map<Integer, Long> commitsByHour = withDate.stream()
                .collect(Collectors.groupingBy(c -> c.committedAt().getHour(), Collectors.counting()));
        Map<String, Long> commitsByDayOfWeek = withDate.stream()
                .collect(Collectors.groupingBy(c -> c.committedAt().getDayOfWeek().name(), Collectors.counting()));

        long weekday = withDate.stream()
                .filter(c -> {
                    DayOfWeek d = c.committedAt().getDayOfWeek();
                    return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
                })
                .count();
        long weekend = withDate.size() - weekday;

        BigDecimal weekdayPct = percentage(weekday, withDate.size());
        BigDecimal weekendPct = percentage(weekend, withDate.size());

        String mostActiveDay = commitsByDayOfWeek.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        Integer mostActiveHour = commitsByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Map<String, Long> commitsByAuthor = allCommits.stream()
                .filter(c -> c.authorLogin() != null)
                .collect(Collectors.groupingBy(CommitRaw::authorLogin, Collectors.counting()));

        long uniqueAuthors = commitsByAuthor.size();

        List<CommitMetricsResult.AuthorStatsResult> topAuthors = commitsByAuthor.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_AUTHORS_LIMIT)
                .map(e -> {
                    String author = e.getKey();
                    long commitCount = e.getValue();
                    long mergeCommitCount = allCommits.stream()
                            .filter(c -> author.equals(c.authorLogin()) && Boolean.TRUE.equals(c.mergeCommit()))
                            .count();
                    return new CommitMetricsResult.AuthorStatsResult(author, commitCount, mergeCommitCount);
                })
                .toList();

        List<Integer> messageLengths = allCommits.stream()
                .mapToInt(CommitRaw::messageLength)
                .filter(len -> len > 0)
                .boxed()
                .toList();
        BigDecimal avgMessageLength = messageLengths.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(messageLengths.stream().mapToInt(i -> i).average().orElse(0.0)).setScale(2, RoundingMode.HALF_UP);
        long longestMessage = messageLengths.isEmpty() ? 0 : messageLengths.stream().mapToLong(i -> i).max().orElse(0);
        long shortestMessage = messageLengths.isEmpty() ? 0 : messageLengths.stream().mapToLong(i -> i).min().orElse(0);

        return new CommitMetricsResult(
                total, mergeCommits, regularCommits, mergeCommitPercentage,
                commitsLast30Days, commitsLast7Days, lastCommitDate, mostActiveDay, mostActiveHour,
                commitsByHour, commitsByDayOfWeek, weekday, weekend, weekdayPct, weekendPct,
                uniqueAuthors, topAuthors, commitsByAuthor,
                avgMessageLength, longestMessage, shortestMessage
        );
    }

    private static CommitMetricsResult emptyResult() {
        return new CommitMetricsResult(
                0L, 0L, 0L, BigDecimal.ZERO,
                0L, 0L, null, null, null,
                Map.of(), Map.of(), 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO,
                0L, List.of(), Map.of(),
                BigDecimal.ZERO, 0L, 0L
        );
    }

    private static BigDecimal percentage(long part, long total) {
        if (total <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(part).divide(BigDecimal.valueOf(total), SCALE, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
