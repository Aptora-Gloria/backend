package com.optimetric.metrics.infrastructure.persistence.mapper;

import com.optimetric.metrics.application.dto.*;
import com.optimetric.metrics.domain.model.*;

import java.math.BigDecimal;
import java.util.List;

public final class UserMetricsPersistenceMapper {

    private UserMetricsPersistenceMapper() {}

    // --- Domain Result → Application Info ---

    public static PrMergeRateInfo toPrMergeRateInfo(PrMetricsResult r) {
        return new PrMergeRateInfo(
                r.totalPullRequests(), r.mergedPullRequests(), r.openPullRequests(), r.closedPullRequests(),
                r.mergeRate(), r.averageMergeTimeHours() != null ? r.averageMergeTimeHours() : BigDecimal.ZERO
        );
    }

    public static PrSizeInfo toPrSizeInfo(PrMetricsResult r) {
        return new PrSizeInfo(
                r.averageAdditions() != null ? r.averageAdditions() : BigDecimal.ZERO,
                r.averageDeletions() != null ? r.averageDeletions() : BigDecimal.ZERO,
                r.averageChangedFiles() != null ? r.averageChangedFiles() : BigDecimal.ZERO,
                r.averageTotalChanges() != null ? r.averageTotalChanges() : BigDecimal.ZERO,
                r.smallPrCount(), r.mediumPrCount(), r.largePrCount(),
                r.smallPrPercentage() != null ? r.smallPrPercentage() : BigDecimal.ZERO,
                r.mediumPrPercentage() != null ? r.mediumPrPercentage() : BigDecimal.ZERO,
                r.largePrPercentage() != null ? r.largePrPercentage() : BigDecimal.ZERO
        );
    }

    public static ReviewStatsInfo toReviewStatsInfo(ReviewMetricsResult r) {
        return new ReviewStatsInfo(
                r.totalReviews(), r.approvedReviews(), r.changesRequestedReviews(), r.commentedReviews(),
                r.averageReviewsPerPr(), r.approvalRate(), r.reviewsByState(),
                r.prsWithoutReviews(), r.prsWithReviewsPercentage()
        );
    }

    public static CommitTimeAnalysisInfo toCommitTimeAnalysisInfo(CommitMetricsResult r) {
        return new CommitTimeAnalysisInfo(
                r.commitsByHour(), r.commitsByDayOfWeek(), r.weekdayCommits(), r.weekendCommits(),
                r.weekdayPercentage(), r.weekendPercentage(), r.mostActiveHour(), r.mostActiveDay(),
                r.commitsLast30Days(), r.commitsLast7Days()
        );
    }

    public static LanguageDiversityInfo toLanguageDiversityInfo(UserLanguageDiversityResult r) {
        List<LanguageDiversityInfo.LanguageUsageInfo> topLanguages = r.topLanguages() == null
                ? List.of()
                : r.topLanguages().stream()
                        .map(s -> new LanguageDiversityInfo.LanguageUsageInfo(
                                s.language(), s.repoCount(), s.totalPercentage(), s.totalBytes()))
                        .toList();
        return new LanguageDiversityInfo(
                r.totalLanguages(), r.uniqueLanguagesAcrossRepos(), r.averageLanguagesPerRepo(),
                r.languageUsageCount(), r.languagePercentageDistribution(), topLanguages,
                r.singleLanguageRepos(), r.multiLanguageRepos(), r.multiLanguageReposPercentage()
        );
    }

    public static ActivityTrendInfo toActivityTrendInfo(ActivityTrendResult r) {
        List<ActivityTrendInfo.WeeklyActivityInfo> weekly = r.weeklyTrend() == null
                ? List.of()
                : r.weeklyTrend().stream()
                        .map(w -> new ActivityTrendInfo.WeeklyActivityInfo(
                                w.week(), w.commits(), w.pullRequests(), w.reviews(), w.totalActivity()))
                        .toList();
        List<ActivityTrendInfo.MonthlyActivityInfo> monthly = r.monthlyTrend() == null
                ? List.of()
                : r.monthlyTrend().stream()
                        .map(m -> new ActivityTrendInfo.MonthlyActivityInfo(
                                m.month(), m.commits(), m.pullRequests(), m.reviews(), m.totalActivity()))
                        .toList();
        ActivityTrendInfo.ActivityBreakdownInfo breakdown = r.last30DaysBreakdown() == null
                ? null
                : new ActivityTrendInfo.ActivityBreakdownInfo(
                r.last30DaysBreakdown().commits(), r.last30DaysBreakdown().pullRequests(), r.last30DaysBreakdown().reviews(),
                r.commitsByDay(), r.prsByDay()
        );
        return new ActivityTrendInfo(weekly, monthly, r.totalActivityLast30Days(), r.totalActivityLast7Days(),
                r.activityGrowthRate(), r.mostActiveWeek(), r.mostActiveMonth(), breakdown);
    }
}
