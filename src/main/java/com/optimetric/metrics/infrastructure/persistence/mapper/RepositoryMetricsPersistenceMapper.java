package com.optimetric.metrics.infrastructure.persistence.mapper;

import com.optimetric.github.application.dto.BranchInfo;
import com.optimetric.github.application.dto.CommitInfo;
import com.optimetric.github.application.dto.LanguageInfo;
import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.metrics.application.dto.*;
import com.optimetric.metrics.domain.model.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class RepositoryMetricsPersistenceMapper {

    private RepositoryMetricsPersistenceMapper() {}

    // PullRequestInfo → PrRaw (domain ham veri).
    public static PrRaw toPrRaw(PullRequestInfo info) {
        var stats = info.changeStats();
        return new PrRaw(
                info.authorLogin(),
                info.state(),
                info.mergedAt(),
                info.createdAt(),
                stats != null ? stats.additions() : null,
                stats != null ? stats.deletions() : null,
                stats != null ? stats.changedFiles() : null,
                info.draft()
        );
    }

    // CommitInfo → CommitRaw (domain ham veri).
    public static CommitRaw toCommitRaw(CommitInfo info) {
        int messageLength = info.message() != null ? info.message().length() : 0;
        return new CommitRaw(
                info.authorLogin(),
                info.committedAt(),
                info.mergeCommit(),
                messageLength
        );
    }

    // BranchInfo → BranchRaw (domain ham veri).
    public static BranchRaw toBranchRaw(BranchInfo info, String defaultBranch) {
        if (info == null) return null;
        long commitCount = info.commitCount() != null ? info.commitCount() : 0L;
        boolean isDefault = defaultBranch != null && defaultBranch.equals(info.name());
        return new BranchRaw(info.name(), commitCount, info.lastCommitDate(), isDefault);
    }

    // ReviewInfo → ReviewRaw (domain ham veri).
    public static ReviewRaw toReviewRaw(ReviewInfo info) {
        String reviewerLogin = info.reviewer() != null ? info.reviewer().login() : null;
        return new ReviewRaw(info.state(), info.submittedAt(), reviewerLogin);
    }

    // LanguageInfo (isim → byte) → LanguageRaw listesi.
    public static List<LanguageRaw> toLanguageRawList(LanguageInfo info) {
        if (info == null || info.languages() == null || info.languages().isEmpty()) {
            return List.of();
        }
        return info.languages().entrySet().stream()
                .map(e -> new LanguageRaw(e.getKey(), Optional.ofNullable(e.getValue()).map(Long::longValue).orElse(0L)))
                .collect(Collectors.toList());
    }

    // Domain sonuç → uygulama info dönüşümleri

    public static RepositoryPrMetricsInfo toPrMetricsInfo(PrMetricsResult r) {
        List<RepositoryPrMetricsInfo.AuthorStatsInfo> topAuthors = r.topAuthors() == null
                ? List.of()
                : r.topAuthors().stream()
                        .map(a -> new RepositoryPrMetricsInfo.AuthorStatsInfo(
                                a.authorLogin(), a.prCount(), a.mergedPrCount(), a.mergeRate()))
                        .toList();
        return new RepositoryPrMetricsInfo(
                r.totalPullRequests(), r.openPullRequests(), r.closedPullRequests(),
                r.mergedPullRequests(), r.draftPullRequests(), r.mergeRate(),
                r.averageAdditions(), r.averageDeletions(), r.averageChangedFiles(),
                r.averageTotalChanges(), r.largestPrSize(), r.smallestPrSize(),
                r.smallPrCount(), r.mediumPrCount(), r.largePrCount(),
                r.smallPrPercentage(), r.mediumPrPercentage(), r.largePrPercentage(),
                r.prsLast30Days(), r.prsLast7Days(), r.mergedLast30Days(),
                r.averageMergeTimeHours(), r.uniqueAuthors(), topAuthors, r.prsByAuthor()
        );
    }

    public static RepositoryCommitMetricsInfo toCommitMetricsInfo(CommitMetricsResult r) {
        List<RepositoryCommitMetricsInfo.AuthorStatsInfo> topAuthors = r.topAuthors() == null
                ? List.of()
                : r.topAuthors().stream()
                        .map(a -> new RepositoryCommitMetricsInfo.AuthorStatsInfo(
                                a.authorLogin(), a.commitCount(), a.mergeCommitCount()))
                        .toList();
        return new RepositoryCommitMetricsInfo(
                r.totalCommits(), r.mergeCommits(), r.regularCommits(), r.mergeCommitPercentage(),
                r.commitsLast30Days(), r.commitsLast7Days(), r.lastCommitDate(), r.mostActiveDay(), r.mostActiveHour(),
                r.commitsByHour(), r.commitsByDayOfWeek(), r.weekdayCommits(), r.weekendCommits(),
                r.weekdayPercentage(), r.weekendPercentage(), r.uniqueAuthors(), topAuthors, r.commitsByAuthor(),
                r.averageMessageLength(), r.longestMessageLength(), r.shortestMessageLength()
        );
    }

    public static RepositoryReviewMetricsInfo toReviewMetricsInfo(ReviewMetricsResult r) {
        List<RepositoryReviewMetricsInfo.ReviewerStatsInfo> topReviewers = r.topReviewers() == null
                ? List.of()
                : r.topReviewers().stream()
                        .map(s -> new RepositoryReviewMetricsInfo.ReviewerStatsInfo(
                                s.reviewerLogin(), s.reviewCount(), s.approvalCount(), s.approvalRate()))
                        .toList();
        return new RepositoryReviewMetricsInfo(
                r.totalReviews(), r.approvedReviews(), r.changesRequestedReviews(), r.commentedReviews(),
                r.reviewsByState(), r.reviewsLast30Days(), r.reviewsLast7Days(), r.averageReviewsPerPr(),
                r.prsWithoutReviews(), r.prsWithReviewsPercentage(), r.approvalRate(),
                r.uniqueReviewers(), topReviewers, r.reviewsByReviewer(), r.approvalsByReviewer()
        );
    }

    public static RepositoryBranchMetricsInfo toBranchMetricsInfo(BranchMetricsResult r) {
        List<RepositoryBranchMetricsInfo.BranchStatsInfo> topBranches = r.topBranchesByCommits() == null
                ? List.of()
                : r.topBranchesByCommits().stream()
                        .map(s -> new RepositoryBranchMetricsInfo.BranchStatsInfo(
                                s.branchName(), s.commitCount(), s.lastCommitDate(), s.isDefault()))
                        .toList();
        return new RepositoryBranchMetricsInfo(
                r.totalBranches(), r.defaultBranch(), topBranches,
                r.commitsByBranch(), r.branchesWithNoCommits(), r.averageCommitsPerBranch()
        );
    }

    public static RepositoryLanguageMetricsInfo toLanguageMetricsInfo(LanguageMetricsResult r) {
        List<RepositoryLanguageMetricsInfo.LanguageStatsInfo> topLanguages = r.topLanguages() == null
                ? List.of()
                : r.topLanguages().stream()
                        .map(s -> new RepositoryLanguageMetricsInfo.LanguageStatsInfo(
                                s.language(), s.bytes(), s.percentage()))
                        .toList();
        return new RepositoryLanguageMetricsInfo(
                r.totalLanguages(), r.primaryLanguage(), r.primaryLanguagePercentage(),
                r.languageDistribution(), r.languageBytes(), topLanguages,
                r.isMultiLanguage(), r.languageDiversityScore()
        );
    }

    public static RepositoryHealthScoreInfo toHealthScoreInfo(HealthScoreResult r) {
        if (r == null) return null;
        RepositoryHealthScoreInfo.HealthBreakdownInfo breakdown = r.breakdown() == null
                ? null
                : new RepositoryHealthScoreInfo.HealthBreakdownInfo(
                r.breakdown().activityWeight(), r.breakdown().popularityWeight(),
                r.breakdown().qualityWeight(), r.breakdown().communityWeight()
        );
        return new RepositoryHealthScoreInfo(
                r.activityScore(), r.popularityScore(), r.codeQualityScore(), r.communityScore(),
                r.overallHealthScore(), r.healthStatus(), breakdown
        );
    }
}
