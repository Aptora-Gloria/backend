package com.optimetric.metrics.api.mapper;

import com.optimetric.metrics.api.response.ActivityTrendDto;
import com.optimetric.metrics.api.response.CommitTimeAnalysisDto;
import com.optimetric.metrics.api.response.LanguageDiversityDto;
import com.optimetric.metrics.api.response.PrMergeRateDto;
import com.optimetric.metrics.api.response.PrSizeDto;
import com.optimetric.metrics.api.response.ReviewStatsDto;
import com.optimetric.metrics.api.response.UserMetricsDto;
import com.optimetric.metrics.application.dto.ActivityTrendInfo;
import com.optimetric.metrics.application.dto.CommitTimeAnalysisInfo;
import com.optimetric.metrics.application.dto.LanguageDiversityInfo;
import com.optimetric.metrics.application.dto.PrMergeRateInfo;
import com.optimetric.metrics.application.dto.PrSizeInfo;
import com.optimetric.metrics.application.dto.ReviewStatsInfo;
import com.optimetric.metrics.application.dto.UserMetricsInfo;

import java.util.List;

public final class UserMetricsApiMapper {

    private UserMetricsApiMapper() {}

    public static UserMetricsDto toUserMetricsDto(UserMetricsInfo info) {
        if (info == null) return null;
        return new UserMetricsDto(
                toPrMergeRateDto(info.prMergeRate()),
                toPrSizeDto(info.prSize()),
                toReviewStatsDto(info.reviewStats()),
                toCommitTimeAnalysisDto(info.commitTimeAnalysis()),
                toLanguageDiversityDto(info.languageDiversity()),
                toActivityTrendDto(info.activityTrend())
        );
    }

    public static PrMergeRateDto toPrMergeRateDto(PrMergeRateInfo info) {
        if (info == null) {
            return null;
        }
        return new PrMergeRateDto(
                info.totalPullRequests(),
                info.mergedPullRequests(),
                info.openPullRequests(),
                info.closedPullRequests(),
                info.mergeRate(),
                info.averageMergeTimeHours()
        );
    }

    public static PrSizeDto toPrSizeDto(PrSizeInfo info) {
        if (info == null) {
            return null;
        }
        return new PrSizeDto(
                info.averageAdditions(),
                info.averageDeletions(),
                info.averageChangedFiles(),
                info.averageTotalChanges(),
                info.smallPrCount(),
                info.mediumPrCount(),
                info.largePrCount(),
                info.smallPrPercentage(),
                info.mediumPrPercentage(),
                info.largePrPercentage()
        );
    }

    public static ReviewStatsDto toReviewStatsDto(ReviewStatsInfo info) {
        if (info == null) {
            return null;
        }
        return new ReviewStatsDto(
                info.totalReviews(),
                info.approvedReviews(),
                info.changesRequestedReviews(),
                info.commentedReviews(),
                info.averageReviewsPerPr(),
                info.approvalRate(),
                info.reviewsByState(),
                info.prsWithoutReviews(),
                info.prsWithReviewsPercentage()
        );
    }

    public static CommitTimeAnalysisDto toCommitTimeAnalysisDto(CommitTimeAnalysisInfo info) {
        if (info == null) {
            return null;
        }
        return new CommitTimeAnalysisDto(
                info.commitsByHour(),
                info.commitsByDayOfWeek(),
                info.weekdayCommits(),
                info.weekendCommits(),
                info.weekdayPercentage(),
                info.weekendPercentage(),
                info.mostActiveHour(),
                info.mostActiveDay(),
                info.last30DaysCommits(),
                info.last7DaysCommits()
        );
    }

    public static LanguageDiversityDto toLanguageDiversityDto(LanguageDiversityInfo info) {
        if (info == null) {
            return null;
        }
        java.util.List<LanguageDiversityDto.LanguageUsageDto> topLanguages = info.topLanguages() == null
                ? List.of()
                : info.topLanguages().stream()
                        .map(s -> new LanguageDiversityDto.LanguageUsageDto(
                                s.language(), s.repoCount(), s.totalPercentage(), s.totalBytes()))
                        .toList();
        return new LanguageDiversityDto(
                info.totalLanguages(),
                info.uniqueLanguagesAcrossRepos(),
                info.averageLanguagesPerRepo(),
                info.languageUsageCount(),
                info.languagePercentageDistribution(),
                topLanguages,
                info.singleLanguageRepos(),
                info.multiLanguageRepos(),
                info.multiLanguageReposPercentage()
        );
    }

    public static ActivityTrendDto toActivityTrendDto(ActivityTrendInfo info) {
        if (info == null) {
            return null;
        }
        java.util.List<ActivityTrendDto.WeeklyActivityDto> weekly = info.weeklyTrend() == null
                ? List.of()
                : info.weeklyTrend().stream()
                        .map(w -> new ActivityTrendDto.WeeklyActivityDto(
                                w.week(), w.commits(), w.pullRequests(), w.reviews(), w.totalActivity()))
                        .toList();
        java.util.List<ActivityTrendDto.MonthlyActivityDto> monthly = info.monthlyTrend() == null
                ? List.of()
                : info.monthlyTrend().stream()
                        .map(m -> new ActivityTrendDto.MonthlyActivityDto(
                                m.month(), m.commits(), m.pullRequests(), m.reviews(), m.totalActivity()))
                        .toList();
        ActivityTrendDto.ActivityBreakdownDto breakdown = info.last30DaysBreakdown() == null
                ? null
                : new ActivityTrendDto.ActivityBreakdownDto(
                info.last30DaysBreakdown().commits(),
                info.last30DaysBreakdown().pullRequests(),
                info.last30DaysBreakdown().reviews(),
                info.last30DaysBreakdown().commitsByDay(),
                info.last30DaysBreakdown().prsByDay()
        );
        return new ActivityTrendDto(
                weekly, monthly,
                info.totalActivityLast30Days(),
                info.totalActivityLast7Days(),
                info.activityGrowthRate(),
                info.mostActiveWeek(),
                info.mostActiveMonth(),
                breakdown
        );
    }
}
