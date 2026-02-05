package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.ActivityTrendInput;
import com.optimetric.metrics.domain.model.ActivityTrendResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Aktivite trendi hesaplayıcı; saf Java, framework yok.
public final class ActivityTrendCalculator {

    private static final int SCALE = 4;

    private ActivityTrendCalculator() {}

    public static ActivityTrendResult calculate(ActivityTrendInput input) {
        List<OffsetDateTime> commits = input.commitDates();
        List<OffsetDateTime> prs = input.prDates();
        List<OffsetDateTime> reviews = input.reviewDates();

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        OffsetDateTime sevenDaysAgo = now.minusDays(7);
        OffsetDateTime sixtyDaysAgo = now.minusDays(60);

        List<ActivityTrendResult.WeeklyActivityResult> weeklyTrend = buildWeeklyTrend(now, commits, prs, reviews);
        List<ActivityTrendResult.MonthlyActivityResult> monthlyTrend = buildMonthlyTrend(now, commits, prs, reviews);

        long total30 = countAfter(commits, thirtyDaysAgo) + countAfter(prs, thirtyDaysAgo) + countAfter(reviews, thirtyDaysAgo);
        long total7 = countAfter(commits, sevenDaysAgo) + countAfter(prs, sevenDaysAgo) + countAfter(reviews, sevenDaysAgo);

        long prev30 = countBetween(commits, sixtyDaysAgo, thirtyDaysAgo) + countBetween(prs, sixtyDaysAgo, thirtyDaysAgo) + countBetween(reviews, sixtyDaysAgo, thirtyDaysAgo);
        BigDecimal growthRate = prev30 > 0
                ? BigDecimal.valueOf(total30 - prev30).divide(BigDecimal.valueOf(prev30), SCALE, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        String mostActiveWeek = weeklyTrend.stream().max(Comparator.comparingLong(ActivityTrendResult.WeeklyActivityResult::totalActivity))
                .map(ActivityTrendResult.WeeklyActivityResult::week).orElse(null);
        String mostActiveMonth = monthlyTrend.stream().max(Comparator.comparingLong(ActivityTrendResult.MonthlyActivityResult::totalActivity))
                .map(ActivityTrendResult.MonthlyActivityResult::month).orElse(null);

        Map<String, Long> commitsByDay = commits.stream().filter(d -> d.isAfter(thirtyDaysAgo))
                .collect(Collectors.groupingBy(d -> d.format(DateTimeFormatter.ISO_LOCAL_DATE), Collectors.counting()));
        Map<String, Long> prsByDay = prs.stream().filter(d -> d.isAfter(thirtyDaysAgo))
                .collect(Collectors.groupingBy(d -> d.format(DateTimeFormatter.ISO_LOCAL_DATE), Collectors.counting()));
        Map<String, Long> reviewsByDay = reviews.stream().filter(d -> d.isAfter(thirtyDaysAgo))
                .collect(Collectors.groupingBy(d -> d.format(DateTimeFormatter.ISO_LOCAL_DATE), Collectors.counting()));

        long c30 = countAfter(commits, thirtyDaysAgo);
        long p30 = countAfter(prs, thirtyDaysAgo);
        long r30 = countAfter(reviews, thirtyDaysAgo);
        ActivityTrendResult.ActivityBreakdownResult breakdown = new ActivityTrendResult.ActivityBreakdownResult(c30, p30, r30);

        return new ActivityTrendResult(
                weeklyTrend, monthlyTrend, total30, total7, breakdown,
                growthRate, mostActiveWeek, mostActiveMonth, commitsByDay, prsByDay, reviewsByDay
        );
    }

    private static long countAfter(List<OffsetDateTime> list, OffsetDateTime after) {
        return list.stream().filter(d -> d.isAfter(after)).count();
    }

    private static long countBetween(List<OffsetDateTime> list, OffsetDateTime start, OffsetDateTime end) {
        return list.stream().filter(d -> d.isAfter(start) && d.isBefore(end)).count();
    }

    private static List<ActivityTrendResult.WeeklyActivityResult> buildWeeklyTrend(OffsetDateTime now, List<OffsetDateTime> commits, List<OffsetDateTime> prs, List<OffsetDateTime> reviews) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-'W'ww");
        List<ActivityTrendResult.WeeklyActivityResult> result = new java.util.ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            OffsetDateTime weekStart = now.minusWeeks(i).with(DayOfWeek.MONDAY);
            OffsetDateTime weekEnd = weekStart.plusDays(6);
            String week = weekStart.format(f);
            long c = commits.stream().filter(d -> !d.isBefore(weekStart) && !d.isAfter(weekEnd)).count();
            long p = prs.stream().filter(d -> !d.isBefore(weekStart) && !d.isAfter(weekEnd)).count();
            long r = reviews.stream().filter(d -> !d.isBefore(weekStart) && !d.isAfter(weekEnd)).count();
            result.add(new ActivityTrendResult.WeeklyActivityResult(week, c, p, r, c + p + r));
        }
        return result;
    }

    private static List<ActivityTrendResult.MonthlyActivityResult> buildMonthlyTrend(OffsetDateTime now, List<OffsetDateTime> commits, List<OffsetDateTime> prs, List<OffsetDateTime> reviews) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM");
        List<ActivityTrendResult.MonthlyActivityResult> result = new java.util.ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            OffsetDateTime monthStart = now.minusMonths(i).withDayOfMonth(1);
            OffsetDateTime monthEnd = monthStart.plusMonths(1).minusDays(1);
            String month = monthStart.format(f);
            long c = commits.stream().filter(d -> !d.isBefore(monthStart) && !d.isAfter(monthEnd)).count();
            long p = prs.stream().filter(d -> !d.isBefore(monthStart) && !d.isAfter(monthEnd)).count();
            long r = reviews.stream().filter(d -> !d.isBefore(monthStart) && !d.isAfter(monthEnd)).count();
            result.add(new ActivityTrendResult.MonthlyActivityResult(month, c, p, r, c + p + r));
        }
        return result;
    }
}
