package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.ReviewMetricsInput;
import com.optimetric.metrics.domain.model.ReviewMetricsResult;
import com.optimetric.metrics.domain.model.ReviewRaw;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// İnceleme metrikleri hesaplayıcı; saf Java.
public final class ReviewMetricsCalculator {

    private static final int SCALE = 4;
    private static final int TOP_REVIEWERS_LIMIT = 10;

    private ReviewMetricsCalculator() {}

    public static ReviewMetricsResult calculate(ReviewMetricsInput input) {
        List<ReviewRaw> reviews = input.reviews();
        long totalPrCount = input.totalPrCount();
        long prsWithoutReviews = input.prsWithoutReviewsCount();

        if (reviews == null || reviews.isEmpty()) {
            return new ReviewMetricsResult(
                    0L, totalPrCount, 0L, 0L, 0L, Map.of(), 0L, 0L, BigDecimal.ZERO,
                    prsWithoutReviews,
                    totalPrCount > 0 ? percentage(totalPrCount - prsWithoutReviews, totalPrCount) : BigDecimal.ZERO,
                    BigDecimal.ZERO, 0L, List.of(), Map.of(), Map.of()
            );
        }

        long total = reviews.size();
        long approved = reviews.stream().filter(r -> "APPROVED".equals(r.state())).count();
        long changesRequested = reviews.stream().filter(r -> "CHANGES_REQUESTED".equals(r.state())).count();
        long commented = reviews.stream().filter(r -> "COMMENTED".equals(r.state())).count();

        Map<String, Long> reviewsByState = reviews.stream()
                .collect(Collectors.groupingBy(r -> r.state() != null ? r.state() : "UNKNOWN", Collectors.counting()));

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        OffsetDateTime sevenDaysAgo = now.minusDays(7);

        long reviewsLast30Days = reviews.stream()
                .filter(r -> r.submittedAt() != null && r.submittedAt().isAfter(thirtyDaysAgo))
                .count();
        long reviewsLast7Days = reviews.stream()
                .filter(r -> r.submittedAt() != null && r.submittedAt().isAfter(sevenDaysAgo))
                .count();

        BigDecimal avgReviewsPerPr = totalPrCount > 0
                ? BigDecimal.valueOf(total).divide(BigDecimal.valueOf(totalPrCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal prsWithReviewsPct = totalPrCount > 0
                ? percentage(totalPrCount - prsWithoutReviews, totalPrCount)
                : BigDecimal.ZERO;
        BigDecimal approvalRate = percentage(approved, total);

        Map<String, Long> reviewsByReviewer = reviews.stream()
                .filter(r -> r.reviewerLogin() != null)
                .collect(Collectors.groupingBy(ReviewRaw::reviewerLogin, Collectors.counting()));
        Map<String, Long> approvalsByReviewer = reviews.stream()
                .filter(r -> r.reviewerLogin() != null && "APPROVED".equals(r.state()))
                .collect(Collectors.groupingBy(ReviewRaw::reviewerLogin, Collectors.counting()));

        long uniqueReviewers = reviewsByReviewer.size();

        List<ReviewMetricsResult.ReviewerStatsResult> topReviewers = reviewsByReviewer.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_REVIEWERS_LIMIT)
                .map(e -> {
                    String reviewer = e.getKey();
                    long reviewCount = e.getValue();
                    long approvalCount = approvalsByReviewer.getOrDefault(reviewer, 0L);
                    BigDecimal rate = percentage(approvalCount, reviewCount);
                    return new ReviewMetricsResult.ReviewerStatsResult(reviewer, reviewCount, approvalCount, rate);
                })
                .toList();

        return new ReviewMetricsResult(
                total, totalPrCount, approved, changesRequested, commented, reviewsByState,
                reviewsLast30Days, reviewsLast7Days, avgReviewsPerPr, prsWithoutReviews, prsWithReviewsPct, approvalRate,
                uniqueReviewers, topReviewers, reviewsByReviewer, approvalsByReviewer
        );
    }

    private static BigDecimal percentage(long part, long total) {
        if (total <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(part).divide(BigDecimal.valueOf(total), SCALE, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
