package com.optimetric.metrics.domain.service;

import com.optimetric.metrics.domain.model.CodeQualityInput;
import com.optimetric.metrics.domain.model.CodeQualityResult;
import com.optimetric.metrics.domain.model.CommitMetricsResult;
import com.optimetric.metrics.domain.model.PrMetricsResult;
import com.optimetric.metrics.domain.model.ReviewMetricsResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Kod kalitesi hesaplayıcı; PR, commit, review kullanır; saf Java.
public final class CodeQualityCalculator {

    private static final int SCALE = 4;

    private CodeQualityCalculator() {}

    public static CodeQualityResult calculate(CodeQualityInput input) {
        PrMetricsResult pr = input.prMetrics();
        CommitMetricsResult commit = input.commitMetrics();
        ReviewMetricsResult review = input.reviewMetrics();

        BigDecimal avgPrSize = pr.averageTotalChanges();
        BigDecimal prMergeRate = pr.mergeRate();
        BigDecimal avgReviewsPerPr = review.averageReviewsPerPr();
        long totalPrs = review.totalPrCount();
        BigDecimal prsWithoutReviewsPct = totalPrs > 0
                ? BigDecimal.valueOf(review.prsWithoutReviews()).divide(BigDecimal.valueOf(totalPrs), SCALE, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        BigDecimal smallPrPct = pr.smallPrPercentage();
        BigDecimal mergeCommitPct = commit.mergeCommitPercentage();
        BigDecimal avgMessageLength = commit.averageMessageLength();
        BigDecimal regularCommitPct = commit.totalCommits() > 0
                ? BigDecimal.valueOf(commit.regularCommits()).divide(BigDecimal.valueOf(commit.totalCommits()), SCALE, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        long totalAdditions = pr.totalAdditions();
        long totalDeletions = pr.totalDeletions();
        long netCodeChange = totalAdditions - totalDeletions;
        long sum = totalAdditions + totalDeletions;
        BigDecimal codeGrowthRate = sum > 0
                ? BigDecimal.valueOf(netCodeChange).divide(BigDecimal.valueOf(sum), SCALE, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        BigDecimal avgFilesChanged = pr.averageChangedFiles();

        BigDecimal qualityScore = BigDecimal.ZERO;
        if (pr.totalPullRequests() > 0 || commit.totalCommits() > 0) {
            BigDecimal prQuality = prMergeRate.multiply(BigDecimal.valueOf(0.4))
                    .add(smallPrPct.multiply(BigDecimal.valueOf(0.2)))
                    .add(avgReviewsPerPr.multiply(BigDecimal.valueOf(0.2)));
            BigDecimal commitQuality = regularCommitPct.multiply(BigDecimal.valueOf(0.3));
            BigDecimal codeChangeQuality = codeGrowthRate.compareTo(BigDecimal.ZERO) > 0
                    ? BigDecimal.valueOf(50).add(codeGrowthRate.multiply(BigDecimal.valueOf(0.5)))
                    : BigDecimal.valueOf(50);
            qualityScore = prQuality.add(commitQuality).add(codeChangeQuality.multiply(BigDecimal.valueOf(0.3)));
            qualityScore = qualityScore.min(BigDecimal.valueOf(100)).max(BigDecimal.ZERO);
        }

        return new CodeQualityResult(
                avgPrSize, prMergeRate, avgReviewsPerPr, prsWithoutReviewsPct, smallPrPct,
                mergeCommitPct, avgMessageLength, regularCommitPct,
                netCodeChange, codeGrowthRate, avgFilesChanged,
                qualityScore
        );
    }
}
