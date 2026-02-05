package com.optimetric.metrics.api.mapper;

import com.optimetric.metrics.api.response.*;
import com.optimetric.metrics.application.dto.*;

import java.util.List;
import java.util.stream.Collectors;

public final class RepositoryMetricsApiMapper {

    private RepositoryMetricsApiMapper() {}

    public static RepositoryMetricsDto toRepositoryMetricsDto(RepositoryMetricsInfo info) {
        if (info == null) return null;
        return new RepositoryMetricsDto(
                toOverviewDto(info.overview()),
                toPrMetricsDto(info.pullRequests()),
                toCommitMetricsDto(info.commits()),
                toReviewMetricsDto(info.reviews()),
                toBranchMetricsDto(info.branches()),
                toLanguageMetricsDto(info.languages()),
                toActivityTrendDto(info.activityTrend()),
                toCodeQualityDto(info.codeQuality()),
                toContributorMetricsDto(info.contributors()),
                toHealthScoreDto(info.healthScore())
        );
    }

    public static RepositoryOverviewDto toOverviewDto(RepositoryOverviewInfo info) {
        if (info == null) {
            return null;
        }
        return new RepositoryOverviewDto(
                info.fullName(),
                info.name(),
                info.description(),
                info.htmlUrl(),
                info.ownerLogin(),
                info.ownerType(),
                info.defaultBranch(),
                info.primaryLanguage(),
                info.stars(),
                info.forks(),
                info.watchers(),
                info.openIssues(),
                info.isPrivate(),
                info.isFork(),
                info.size(),
                info.createdAt(),
                info.updatedAt(),
                info.pushedAt(),
                info.lastSyncedAt(),
                info.repositoryAgeDays(),
                info.isActive(),
                info.daysSinceLastPush()
        );
    }

    public static RepositoryLanguageMetricsDto toLanguageMetricsDto(RepositoryLanguageMetricsInfo info) {
        if (info == null) {
            return null;
        }
        List<RepositoryLanguageMetricsDto.LanguageStatsDto> topLanguages = info.topLanguages() == null
                ? List.of()
                : info.topLanguages().stream()
                        .map(s -> new RepositoryLanguageMetricsDto.LanguageStatsDto(s.language(), s.bytes(), s.percentage()))
                        .collect(Collectors.toList());
        return new RepositoryLanguageMetricsDto(
                info.totalLanguages(),
                info.primaryLanguage(),
                info.primaryLanguagePercentage(),
                info.languageDistribution(),
                info.languageBytes(),
                topLanguages,
                info.isMultiLanguage(),
                info.languageDiversityScore()
        );
    }

    public static RepositoryHealthScoreDto toHealthScoreDto(RepositoryHealthScoreInfo info) {
        if (info == null) {
            return null;
        }
        RepositoryHealthScoreDto.HealthBreakdownDto breakdown = info.breakdown() == null
                ? null
                : new RepositoryHealthScoreDto.HealthBreakdownDto(
                info.breakdown().activityWeight(),
                info.breakdown().popularityWeight(),
                info.breakdown().qualityWeight(),
                info.breakdown().communityWeight()
        );
        return new RepositoryHealthScoreDto(
                info.activityScore(),
                info.popularityScore(),
                info.codeQualityScore(),
                info.communityScore(),
                info.overallHealthScore(),
                info.healthStatus(),
                breakdown
        );
    }

    public static RepositoryReviewMetricsDto toReviewMetricsDto(RepositoryReviewMetricsInfo info) {
        if (info == null) {
            return null;
        }
        java.util.List<RepositoryReviewMetricsDto.ReviewerStatsDto> topReviewers = info.topReviewers() == null
                ? List.of()
                : info.topReviewers().stream()
                        .map(s -> new RepositoryReviewMetricsDto.ReviewerStatsDto(
                                s.reviewerLogin(), s.reviewCount(), s.approvalCount(), s.approvalRate()))
                        .collect(Collectors.toList());
        return new RepositoryReviewMetricsDto(
                info.totalReviews(),
                info.approvedReviews(),
                info.changesRequestedReviews(),
                info.commentedReviews(),
                info.reviewsByState(),
                info.reviewsLast30Days(),
                info.reviewsLast7Days(),
                info.averageReviewsPerPr(),
                info.prsWithoutReviews(),
                info.prsWithReviewsPercentage(),
                info.approvalRate(),
                info.uniqueReviewers(),
                topReviewers,
                info.reviewsByReviewer(),
                info.approvalsByReviewer()
        );
    }

    public static RepositoryBranchMetricsDto toBranchMetricsDto(RepositoryBranchMetricsInfo info) {
        if (info == null) {
            return null;
        }
        List<RepositoryBranchMetricsDto.BranchStatsDto> topBranches = info.topBranchesByCommits() == null
                ? List.of()
                : info.topBranchesByCommits().stream()
                        .map(s -> new RepositoryBranchMetricsDto.BranchStatsDto(
                                s.branchName(), s.commitCount(), s.lastCommitDate(), s.isDefault()))
                        .collect(Collectors.toList());
        return new RepositoryBranchMetricsDto(
                info.totalBranches(),
                info.defaultBranch(),
                topBranches,
                info.commitsByBranch(),
                info.branchesWithNoCommits(),
                info.averageCommitsPerBranch()
        );
    }

    public static RepositoryPrMetricsDto toPrMetricsDto(RepositoryPrMetricsInfo info) {
        if (info == null) {
            return null;
        }
        List<RepositoryPrMetricsDto.AuthorStatsDto> topAuthors = info.topAuthors() == null
                ? List.of()
                : info.topAuthors().stream()
                        .map(s -> new RepositoryPrMetricsDto.AuthorStatsDto(
                                s.authorLogin(), s.prCount(), s.mergedPrCount(), s.mergeRate()))
                        .collect(Collectors.toList());
        return new RepositoryPrMetricsDto(
                info.totalPullRequests(), info.openPullRequests(), info.closedPullRequests(),
                info.mergedPullRequests(), info.draftPullRequests(), info.mergeRate(),
                info.averageAdditions(), info.averageDeletions(), info.averageChangedFiles(),
                info.averageTotalChanges(), info.largestPrSize(), info.smallestPrSize(),
                info.smallPrCount(), info.mediumPrCount(), info.largePrCount(),
                info.smallPrPercentage(), info.mediumPrPercentage(), info.largePrPercentage(),
                info.prsLast30Days(), info.prsLast7Days(), info.mergedLast30Days(),
                info.averageMergeTimeHours(), info.uniqueAuthors(), topAuthors, info.prsByAuthor()
        );
    }

    public static RepositoryCommitMetricsDto toCommitMetricsDto(RepositoryCommitMetricsInfo info) {
        if (info == null) {
            return null;
        }
        List<RepositoryCommitMetricsDto.AuthorStatsDto> topAuthors = info.topAuthors() == null
                ? List.of()
                : info.topAuthors().stream()
                        .map(s -> new RepositoryCommitMetricsDto.AuthorStatsDto(
                                s.authorLogin(), s.commitCount(), s.mergeCommitCount()))
                        .collect(Collectors.toList());
        return new RepositoryCommitMetricsDto(
                info.totalCommits(), info.mergeCommits(), info.regularCommits(),
                info.mergeCommitPercentage(), info.commitsLast30Days(), info.commitsLast7Days(),
                info.lastCommitDate(), info.mostActiveDay(), info.mostActiveHour(),
                info.commitsByHour(), info.commitsByDayOfWeek(), info.weekdayCommits(),
                info.weekendCommits(), info.weekdayPercentage(), info.weekendPercentage(),
                info.uniqueAuthors(), topAuthors, info.commitsByAuthor(),
                info.averageMessageLength(), info.longestMessageLength(), info.shortestMessageLength()
        );
    }

    public static RepositoryActivityTrendDto toActivityTrendDto(RepositoryActivityTrendInfo info) {
        if (info == null) {
            return null;
        }
        List<RepositoryActivityTrendDto.WeeklyActivityDto> weeklyTrend = info.weeklyTrend() == null
                ? List.of()
                : info.weeklyTrend().stream()
                        .map(w -> new RepositoryActivityTrendDto.WeeklyActivityDto(
                                w.week(), w.commits(), w.pullRequests(), w.reviews(), w.totalActivity()))
                        .collect(Collectors.toList());
        List<RepositoryActivityTrendDto.MonthlyActivityDto> monthlyTrend = info.monthlyTrend() == null
                ? List.of()
                : info.monthlyTrend().stream()
                        .map(m -> new RepositoryActivityTrendDto.MonthlyActivityDto(
                                m.month(), m.commits(), m.pullRequests(), m.reviews(), m.totalActivity()))
                        .collect(Collectors.toList());
        RepositoryActivityTrendDto.ActivityBreakdownDto breakdown = info.last30DaysBreakdown() == null
                ? null
                : new RepositoryActivityTrendDto.ActivityBreakdownDto(
                info.last30DaysBreakdown().commits(),
                info.last30DaysBreakdown().pullRequests(),
                info.last30DaysBreakdown().reviews()
        );
        return new RepositoryActivityTrendDto(
                weeklyTrend, monthlyTrend, info.totalActivityLast30Days(), info.totalActivityLast7Days(),
                breakdown, info.activityGrowthRate(), info.mostActiveWeek(), info.mostActiveMonth(),
                info.commitsByDay(), info.prsByDay(), info.reviewsByDay()
        );
    }

    public static RepositoryCodeQualityDto toCodeQualityDto(RepositoryCodeQualityInfo info) {
        if (info == null) {
            return null;
        }
        return new RepositoryCodeQualityDto(
                info.averagePrSize(), info.prMergeRate(), info.averageReviewsPerPr(),
                info.prsWithoutReviewsPercentage(), info.smallPrPercentage(),
                info.mergeCommitPercentage(), info.averageCommitMessageLength(),
                info.regularCommitPercentage(), info.netCodeChange(), info.codeGrowthRate(),
                info.averageFilesChangedPerPr(), info.overallQualityScore()
        );
    }

    public static RepositoryContributorMetricsDto toContributorMetricsDto(RepositoryContributorMetricsInfo info) {
        if (info == null) {
            return null;
        }
        List<RepositoryContributorMetricsDto.ContributorStatsDto> topByCommits = info.topContributorsByCommits() == null
                ? List.of()
                : info.topContributorsByCommits().stream()
                        .map(s -> new RepositoryContributorMetricsDto.ContributorStatsDto(
                                s.contributorLogin(), s.commitCount(), s.prCount(), s.reviewCount(), s.totalContributions()))
                        .collect(Collectors.toList());
        List<RepositoryContributorMetricsDto.ContributorStatsDto> topByPrs = info.topContributorsByPrs() == null
                ? List.of()
                : info.topContributorsByPrs().stream()
                        .map(s -> new RepositoryContributorMetricsDto.ContributorStatsDto(
                                s.contributorLogin(), s.commitCount(), s.prCount(), s.reviewCount(), s.totalContributions()))
                        .collect(Collectors.toList());
        List<RepositoryContributorMetricsDto.ContributorStatsDto> topByReviews = info.topContributorsByReviews() == null
                ? List.of()
                : info.topContributorsByReviews().stream()
                        .map(s -> new RepositoryContributorMetricsDto.ContributorStatsDto(
                                s.contributorLogin(), s.commitCount(), s.prCount(), s.reviewCount(), s.totalContributions()))
                        .collect(Collectors.toList());
        return new RepositoryContributorMetricsDto(
                info.totalContributors(), info.activeContributorsLast30Days(), info.activeContributorsLast7Days(),
                topByCommits, topByPrs, topByReviews,
                info.commitsByContributor(), info.prsByContributor(), info.reviewsByContributor(),
                info.top10ContributorsPercentage(), info.singleCommitContributors(), info.regularContributors()
        );
    }
}
