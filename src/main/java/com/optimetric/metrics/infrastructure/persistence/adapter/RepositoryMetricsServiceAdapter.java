package com.optimetric.metrics.infrastructure.persistence.adapter;

import com.optimetric.github.application.dto.CommitInfo;
import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.github.application.port.in.GetBranchesPort;
import com.optimetric.github.application.port.in.GetCommitsPort;
import com.optimetric.github.application.port.in.GetLanguagesPort;
import com.optimetric.github.application.port.in.GetPullRequestReviewsPort;
import com.optimetric.github.application.port.in.GetPullRequestsPort;
import com.optimetric.github.application.usecase.GetRepositoryForCurrentUserUseCase;
import com.optimetric.metrics.application.dto.RepositoryActivityTrendInfo;
import com.optimetric.metrics.application.dto.RepositoryBranchMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryCodeQualityInfo;
import com.optimetric.metrics.application.dto.RepositoryCommitMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryContributorMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryHealthScoreInfo;
import com.optimetric.metrics.application.dto.RepositoryLanguageMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryOverviewInfo;
import com.optimetric.metrics.application.dto.RepositoryPrMetricsInfo;
import com.optimetric.metrics.application.dto.RepositoryReviewMetricsInfo;
import com.optimetric.metrics.application.port.RepositoryMetricsServicePort;
import com.optimetric.metrics.domain.model.BranchMetricsInput;
import com.optimetric.metrics.domain.model.BranchMetricsResult;
import com.optimetric.metrics.domain.model.BranchRaw;
import com.optimetric.metrics.domain.model.CommitMetricsInput;
import com.optimetric.metrics.domain.model.CommitMetricsResult;
import com.optimetric.metrics.domain.model.CommitRaw;
import com.optimetric.metrics.domain.model.LanguageMetricsInput;
import com.optimetric.metrics.domain.model.LanguageMetricsResult;
import com.optimetric.metrics.domain.model.LanguageRaw;
import com.optimetric.metrics.domain.model.PrMetricsInput;
import com.optimetric.metrics.domain.model.PrMetricsResult;
import com.optimetric.metrics.domain.model.PrRaw;
import com.optimetric.metrics.domain.model.ActivityTrendInput;
import com.optimetric.metrics.domain.model.ActivityTrendResult;
import com.optimetric.metrics.domain.model.ContributorMetricsInput;
import com.optimetric.metrics.domain.model.ContributorMetricsResult;
import com.optimetric.metrics.domain.model.HealthScoreInput;
import com.optimetric.metrics.domain.model.HealthScoreResult;
import com.optimetric.metrics.domain.model.OverviewInput;
import com.optimetric.metrics.domain.model.ReviewMetricsInput;
import com.optimetric.metrics.domain.model.ReviewMetricsResult;
import com.optimetric.metrics.domain.model.ReviewRaw;
import com.optimetric.metrics.domain.service.ActivityTrendCalculator;
import com.optimetric.metrics.domain.service.BranchMetricsCalculator;
import com.optimetric.metrics.domain.service.CommitMetricsCalculator;
import com.optimetric.metrics.domain.service.ContributorMetricsCalculator;
import com.optimetric.metrics.domain.service.HealthScoreCalculator;
import com.optimetric.metrics.domain.service.LanguageMetricsCalculator;
import com.optimetric.metrics.domain.service.PrMetricsCalculator;
import com.optimetric.metrics.domain.service.ReviewMetricsCalculator;
import com.optimetric.metrics.application.port.out.RepositoryMetricsDataPort;
import com.optimetric.metrics.application.port.out.RepositoryMetricsQueryPort;
import com.optimetric.metrics.infrastructure.persistence.mapper.RepositoryMetricsPersistenceMapper;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryMetricsServiceAdapter implements RepositoryMetricsServicePort, RepositoryMetricsQueryPort, RepositoryMetricsDataPort {

    private final GetRepositoryForCurrentUserUseCase getRepositoryForCurrentUserUseCase;
    private final GetPullRequestsPort getPullRequestsPort;
    private final GetPullRequestReviewsPort getPullRequestReviewsPort;
    private final GetLanguagesPort getLanguagesPort;
    private final GetCommitsPort getCommitsPort;
    private final GetBranchesPort getBranchesPort;

    @Override
    @Transactional(readOnly = true)
    public RepositoryMetricsInfo getRepositoryMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return new RepositoryMetricsInfo(
                getOverview(login, repoFullName),
                getPullRequestMetrics(login, repoFullName),
                getCommitMetrics(login, repoFullName),
                getReviewMetrics(login, repoFullName),
                getBranchMetrics(login, repoFullName),
                getLanguageMetrics(login, repoFullName),
                getActivityTrend(login, repoFullName),
                getCodeQuality(login, repoFullName),
                getContributorMetrics(login, repoFullName),
                getHealthScore(login, repoFullName)
        );
    }

    private RepositoryInfo getRepositoryInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getRepositoryForCurrentUserUseCase.executeOrThrow(login, repoFullName);
    }

    // PR listesini GitHub uygulama portu ile çeker (JPA kullanmaz).
    private List<PullRequestInfo> getPullRequestsForRepo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getPullRequestsPort.getPullRequestsForRepository(login, repoFullName);
    }

    // Repo commit'lerini GitHub uygulama portu ile çeker (branch filtresi).
    private List<CommitInfo> getCommitsForRepo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getCommitsPort.getCommits(login, repoFullName, null);
    }

    @Override
    @Transactional(readOnly = true)
    public OverviewInput getOverviewInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryInfo info = getRepositoryInfo(login, repoFullName);
        return new OverviewInput(
                info.fullName(),
                info.name(),
                info.description(),
                info.htmlUrl(),
                info.ownerLogin(),
                info.ownerType() != null ? info.ownerType().name() : null,
                info.defaultBranch(),
                info.language(),
                info.stargazersCount(),
                info.forksCount(),
                info.watchersCount(),
                info.openIssuesCount(),
                info.isPrivate(),
                info.isFork(),
                info.size(),
                info.createdAt(),
                info.updatedAt(),
                info.pushedAt(),
                info.lastSyncedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryOverviewInfo getOverviewInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryInfo info = getRepositoryInfo(login, repoFullName);
        return buildOverviewInfo(info);
    }

    private RepositoryOverviewInfo buildOverviewInfo(RepositoryInfo info) {
        OffsetDateTime now = OffsetDateTime.now();
        long ageDays = info.createdAt() != null
                ? Duration.between(info.createdAt(), now).toDays()
                : 0;
        boolean isActive = info.pushedAt() != null
                && info.pushedAt().isAfter(now.minusDays(30));
        long daysSinceLastPush = info.pushedAt() != null
                ? Duration.between(info.pushedAt(), now).toDays()
                : 0;
        return new RepositoryOverviewInfo(
                info.fullName(),
                info.name(),
                info.description(),
                info.htmlUrl(),
                info.ownerLogin(),
                info.ownerType() != null ? info.ownerType().name() : null,
                info.defaultBranch(),
                info.language(),
                info.stargazersCount(),
                info.forksCount(),
                info.watchersCount(),
                info.openIssuesCount(),
                info.isPrivate(),
                info.isFork(),
                info.size(),
                info.createdAt(),
                info.updatedAt(),
                info.pushedAt(),
                info.lastSyncedAt(),
                ageDays,
                isActive,
                daysSinceLastPush
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryOverviewInfo getOverview(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryInfo info = getRepositoryInfo(login, repoFullName);
        return buildOverviewInfo(info);
    }

    @Override
    @Transactional(readOnly = true)
    public PrMetricsInput getPrMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        List<PullRequestInfo> prs = getPullRequestsForRepo(login, repoFullName);
        List<PrRaw> rawList = prs.stream()
                .map(RepositoryMetricsPersistenceMapper::toPrRaw)
                .toList();
        return new PrMetricsInput(rawList);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryPrMetricsInfo getPrMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        PrMetricsInput input = getPrMetricsInput(login, repoFullName);
        PrMetricsResult result = PrMetricsCalculator.calculate(input);
        return RepositoryMetricsPersistenceMapper.toPrMetricsInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryPrMetricsInfo getPullRequestMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getPrMetricsInfo(login, repoFullName);
    }

    @Override
    @Transactional(readOnly = true)
    public CommitMetricsInput getCommitMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        getRepositoryInfo(login, repoFullName);
        List<CommitInfo> commits = getCommitsForRepo(login, repoFullName);
        List<CommitRaw> rawList = commits.stream()
                .map(RepositoryMetricsPersistenceMapper::toCommitRaw)
                .toList();
        return new CommitMetricsInput(rawList);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryCommitMetricsInfo getCommitMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        CommitMetricsInput input = getCommitMetricsInput(login, repoFullName);
        CommitMetricsResult result = CommitMetricsCalculator.calculate(input);
        return RepositoryMetricsPersistenceMapper.toCommitMetricsInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryCommitMetricsInfo getCommitMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getCommitMetricsInfo(login, repoFullName);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewMetricsInput getReviewMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        List<PullRequestInfo> allPrs = getPullRequestsForRepo(login, repoFullName);
        List<ReviewRaw> reviews = allPrs.stream()
                .flatMap(pr -> getPullRequestReviewsPort.getPullRequestReviews(login, repoFullName, pr.prNumber()).stream())
                .map(RepositoryMetricsPersistenceMapper::toReviewRaw)
                .toList();
        long totalPrCount = allPrs.size();
        long prsWithoutReviews = allPrs.stream()
                .filter(pr -> getPullRequestReviewsPort.getPullRequestReviews(login, repoFullName, pr.prNumber()).isEmpty())
                .count();
        return new ReviewMetricsInput(reviews, totalPrCount, prsWithoutReviews);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryReviewMetricsInfo getReviewMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        ReviewMetricsInput input = getReviewMetricsInput(login, repoFullName);
        ReviewMetricsResult result = ReviewMetricsCalculator.calculate(input);
        return RepositoryMetricsPersistenceMapper.toReviewMetricsInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryReviewMetricsInfo getReviewMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getReviewMetricsInfo(login, repoFullName);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchMetricsInput getBranchMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryInfo repoInfo = getRepositoryInfo(login, repoFullName);
        String defaultBranch = repoInfo.defaultBranch();
        List<BranchRaw> branches = getBranchesPort.getBranches(login, repoFullName).stream()
                .map(b -> RepositoryMetricsPersistenceMapper.toBranchRaw(b, defaultBranch))
                .toList();
        return new BranchMetricsInput(defaultBranch, branches);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryBranchMetricsInfo getBranchMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        BranchMetricsInput input = getBranchMetricsInput(login, repoFullName);
        BranchMetricsResult result = BranchMetricsCalculator.calculate(input);
        return RepositoryMetricsPersistenceMapper.toBranchMetricsInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryBranchMetricsInfo getBranchMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getBranchMetricsInfo(login, repoFullName);
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageMetricsInput getLanguageMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        getRepositoryInfo(login, repoFullName);
        var languageInfo = getLanguagesPort.getLanguages(repoFullName);
        List<LanguageRaw> rawList = RepositoryMetricsPersistenceMapper.toLanguageRawList(languageInfo);
        return new LanguageMetricsInput(rawList);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryLanguageMetricsInfo getLanguageMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        LanguageMetricsInput input = getLanguageMetricsInput(login, repoFullName);
        LanguageMetricsResult result = LanguageMetricsCalculator.calculate(input);
        return RepositoryMetricsPersistenceMapper.toLanguageMetricsInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryLanguageMetricsInfo getLanguageMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getLanguageMetricsInfo(login, repoFullName);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityTrendInput getActivityTrendInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        OffsetDateTime twelveWeeksAgo = OffsetDateTime.now().minusWeeks(12);
        List<CommitInfo> commits = getCommitsForRepo(login, repoFullName).stream()
                .filter(c -> c.committedAt() != null && c.committedAt().isAfter(twelveWeeksAgo))
                .toList();
        List<PullRequestInfo> prs = getPullRequestsForRepo(login, repoFullName).stream()
                .filter(pr -> pr.createdAt() != null && pr.createdAt().isAfter(twelveWeeksAgo))
                .toList();
        List<OffsetDateTime> reviewDates = prs.stream()
                .flatMap(pr -> getPullRequestReviewsPort.getPullRequestReviews(login, repoFullName, pr.prNumber()).stream())
                .map(ReviewInfo::submittedAt)
                .filter(Objects::nonNull)
                .filter(dt -> dt.isAfter(twelveWeeksAgo))
                .toList();
        List<OffsetDateTime> commitDates = commits.stream()
                .map(CommitInfo::committedAt)
                .filter(Objects::nonNull)
                .toList();
        List<OffsetDateTime> prDates = prs.stream()
                .map(PullRequestInfo::createdAt)
                .filter(Objects::nonNull)
                .toList();
        return new ActivityTrendInput(commitDates, prDates, reviewDates);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryActivityTrendInfo getActivityTrendInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime twelveWeeksAgo = now.minusWeeks(12);
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        OffsetDateTime sevenDaysAgo = now.minusDays(7);

        List<CommitInfo> commits = getCommitsForRepo(login, repoFullName).stream()
            .filter(c -> c.committedAt() != null && c.committedAt().isAfter(twelveWeeksAgo))
            .toList();

        List<PullRequestInfo> prs = getPullRequestsForRepo(login, repoFullName).stream()
            .filter(pr -> pr.createdAt() != null && pr.createdAt().isAfter(twelveWeeksAgo))
            .toList();

        List<ReviewInfo> reviews = prs.stream()
            .flatMap(pr -> getPullRequestReviewsPort.getPullRequestReviews(login, repoFullName, pr.prNumber()).stream())
            .filter(r -> r.submittedAt() != null && r.submittedAt().isAfter(twelveWeeksAgo))
            .toList();

        List<RepositoryActivityTrendInfo.WeeklyActivityInfo> weeklyTrend = new ArrayList<>();
        DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("yyyy-'W'ww");
        for (int i = 11; i >= 0; i--) {
            OffsetDateTime weekStart = now.minusWeeks(i).with(DayOfWeek.MONDAY);
            OffsetDateTime weekEnd = weekStart.plusDays(6);
            String week = weekStart.format(weekFormatter);
            long weekCommits = commits.stream()
                .filter(c -> c.committedAt() != null && !c.committedAt().isBefore(weekStart) && !c.committedAt().isAfter(weekEnd))
                .count();
            long weekPrs = prs.stream()
                .filter(pr -> !pr.createdAt().isBefore(weekStart) && !pr.createdAt().isAfter(weekEnd))
                .count();
            long weekReviews = reviews.stream()
                .filter(r -> !r.submittedAt().isBefore(weekStart) && !r.submittedAt().isAfter(weekEnd))
                .count();
            weeklyTrend.add(new RepositoryActivityTrendInfo.WeeklyActivityInfo(
                week, weekCommits, weekPrs, weekReviews, weekCommits + weekPrs + weekReviews));
        }

        List<RepositoryActivityTrendInfo.MonthlyActivityInfo> monthlyTrend = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            OffsetDateTime monthStart = now.minusMonths(i).withDayOfMonth(1);
            OffsetDateTime monthEnd = monthStart.plusMonths(1).minusDays(1);
            String month = monthStart.format(monthFormatter);
            long monthCommits = commits.stream()
                .filter(c -> c.committedAt() != null && !c.committedAt().isBefore(monthStart) && !c.committedAt().isAfter(monthEnd))
                .count();
            long monthPrs = prs.stream()
                .filter(pr -> !pr.createdAt().isBefore(monthStart) && !pr.createdAt().isAfter(monthEnd))
                .count();
            long monthReviews = reviews.stream()
                .filter(r -> !r.submittedAt().isBefore(monthStart) && !r.submittedAt().isAfter(monthEnd))
                .count();
            monthlyTrend.add(new RepositoryActivityTrendInfo.MonthlyActivityInfo(
                month, monthCommits, monthPrs, monthReviews, monthCommits + monthPrs + monthReviews));
        }

        long total30Days = commits.stream().filter(c -> c.committedAt() != null && c.committedAt().isAfter(thirtyDaysAgo)).count() +
            prs.stream().filter(pr -> pr.createdAt().isAfter(thirtyDaysAgo)).count() +
            reviews.stream().filter(r -> r.submittedAt().isAfter(thirtyDaysAgo)).count();
        long total7Days = commits.stream().filter(c -> c.committedAt() != null && c.committedAt().isAfter(sevenDaysAgo)).count() +
            prs.stream().filter(pr -> pr.createdAt().isAfter(sevenDaysAgo)).count() +
            reviews.stream().filter(r -> r.submittedAt().isAfter(sevenDaysAgo)).count();

        OffsetDateTime sixtyDaysAgo = now.minusDays(60);
        long previous30Days = commits.stream()
                .filter(c -> c.committedAt() != null && c.committedAt().isAfter(sixtyDaysAgo) && c.committedAt().isBefore(thirtyDaysAgo)).count() +
            prs.stream().filter(pr -> pr.createdAt().isAfter(sixtyDaysAgo) && pr.createdAt().isBefore(thirtyDaysAgo)).count() +
            reviews.stream().filter(r -> r.submittedAt().isAfter(sixtyDaysAgo) && r.submittedAt().isBefore(thirtyDaysAgo)).count();
        BigDecimal growthRate = previous30Days > 0
            ? BigDecimal.valueOf(total30Days - previous30Days).divide(BigDecimal.valueOf(previous30Days), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;

        String mostActiveWeek = weeklyTrend.stream()
            .max(Comparator.comparing(RepositoryActivityTrendInfo.WeeklyActivityInfo::totalActivity))
            .map(RepositoryActivityTrendInfo.WeeklyActivityInfo::week)
            .orElse(null);
        String mostActiveMonth = monthlyTrend.stream()
            .max(Comparator.comparing(RepositoryActivityTrendInfo.MonthlyActivityInfo::totalActivity))
            .map(RepositoryActivityTrendInfo.MonthlyActivityInfo::month)
            .orElse(null);

        Map<String, Long> commitsByDay = commits.stream()
            .filter(c -> c.committedAt() != null && c.committedAt().isAfter(thirtyDaysAgo))
            .collect(Collectors.groupingBy(
                c -> c.committedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                Collectors.counting()
            ));
        Map<String, Long> prsByDay = prs.stream()
            .filter(pr -> pr.createdAt().isAfter(thirtyDaysAgo))
            .collect(Collectors.groupingBy(
                pr -> pr.createdAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                Collectors.counting()
            ));
        Map<String, Long> reviewsByDay = reviews.stream()
            .filter(r -> r.submittedAt().isAfter(thirtyDaysAgo))
            .collect(Collectors.groupingBy(
                r -> r.submittedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                Collectors.counting()
            ));

        long commits30 = commits.stream().filter(c -> c.committedAt() != null && c.committedAt().isAfter(thirtyDaysAgo)).count();
        long prs30 = prs.stream().filter(pr -> pr.createdAt().isAfter(thirtyDaysAgo)).count();
        long reviews30 = reviews.stream().filter(r -> r.submittedAt().isAfter(thirtyDaysAgo)).count();
        RepositoryActivityTrendInfo.ActivityBreakdownInfo breakdown =
            new RepositoryActivityTrendInfo.ActivityBreakdownInfo(commits30, prs30, reviews30);

        return new RepositoryActivityTrendInfo(
            weeklyTrend, monthlyTrend, total30Days, total7Days, breakdown,
            growthRate, mostActiveWeek, mostActiveMonth, commitsByDay, prsByDay, reviewsByDay
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryActivityTrendInfo getActivityTrend(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getActivityTrendInfo(login, repoFullName);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryCodeQualityInfo getCodeQualityInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        List<PullRequestInfo> allPrs = getPullRequestsForRepo(login, repoFullName);
        List<CommitInfo> allCommits = getCommitsForRepo(login, repoFullName);

        List<PullRequestInfo> prsWithStats = allPrs.stream()
            .filter(pr -> pr.changeStats() != null && pr.changeStats().additions() != null && pr.changeStats().deletions() != null)
            .toList();

        BigDecimal avgPrSize = prsWithStats.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(prsWithStats.stream()
                .mapToInt(pr -> pr.changeStats().additions() + pr.changeStats().deletions())
                .average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);

        long merged = allPrs.stream().filter(pr -> pr.mergedAt() != null).count();
        BigDecimal prMergeRate = allPrs.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(merged).divide(BigDecimal.valueOf(allPrs.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        long totalReviews = allPrs.stream()
            .mapToLong(pr -> getPullRequestReviewsPort.getPullRequestReviews(login, repoFullName, pr.prNumber()).size())
            .sum();
        BigDecimal avgReviewsPerPr = allPrs.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(totalReviews).divide(BigDecimal.valueOf(allPrs.size()), 2, RoundingMode.HALF_UP);

        long prsWithoutReviews = allPrs.stream()
            .filter(pr -> getPullRequestReviewsPort.getPullRequestReviews(login, repoFullName, pr.prNumber()).isEmpty())
            .count();
        BigDecimal prsWithoutReviewsPct = allPrs.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(prsWithoutReviews).divide(BigDecimal.valueOf(allPrs.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        long smallPrs = prsWithStats.stream()
            .filter(pr -> (pr.changeStats().additions() + pr.changeStats().deletions()) < 100)
            .count();
        BigDecimal smallPrPct = prsWithStats.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(smallPrs).divide(BigDecimal.valueOf(prsWithStats.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        long mergeCommits = allCommits.stream().filter(c -> Boolean.TRUE.equals(c.mergeCommit())).count();
        BigDecimal mergeCommitPct = allCommits.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(mergeCommits).divide(BigDecimal.valueOf(allCommits.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        List<Integer> messageLengths = allCommits.stream()
            .filter(c -> c.message() != null)
            .map(c -> c.message().length())
            .toList();
        BigDecimal avgMessageLength = messageLengths.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(messageLengths.stream().mapToInt(i -> i).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal regularCommitPct = allCommits.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(allCommits.size() - mergeCommits)
                .divide(BigDecimal.valueOf(allCommits.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        long totalAdditions = prsWithStats.stream()
            .mapToInt(pr -> Optional.ofNullable(pr.changeStats()).map(c -> Optional.ofNullable(c.additions()).orElse(0)).orElse(0))
            .sum();
        long totalDeletions = prsWithStats.stream()
            .mapToInt(pr -> Optional.ofNullable(pr.changeStats()).map(c -> Optional.ofNullable(c.deletions()).orElse(0)).orElse(0))
            .sum();
        long netCodeChange = totalAdditions - totalDeletions;

        BigDecimal codeGrowthRate = (totalAdditions + totalDeletions) > 0
            ? BigDecimal.valueOf(netCodeChange).divide(BigDecimal.valueOf(totalAdditions + totalDeletions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;

        BigDecimal avgFilesChanged = prsWithStats.stream()
            .filter(pr -> pr.changeStats() != null && pr.changeStats().changedFiles() != null)
            .mapToInt(pr -> pr.changeStats().changedFiles())
            .average()
            .stream()
            .mapToObj(avg -> BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
            .findFirst()
            .orElse(BigDecimal.ZERO);

        BigDecimal qualityScore = BigDecimal.ZERO;
        if (!allPrs.isEmpty() || !allCommits.isEmpty()) {
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

        return new RepositoryCodeQualityInfo(
            avgPrSize, prMergeRate, avgReviewsPerPr, prsWithoutReviewsPct, smallPrPct,
            mergeCommitPct, avgMessageLength, regularCommitPct,
            netCodeChange, codeGrowthRate, avgFilesChanged,
            qualityScore
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryCodeQualityInfo getCodeQuality(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getCodeQualityInfo(login, repoFullName);
    }

    @Override
    @Transactional(readOnly = true)
    public ContributorMetricsInput getContributorMetricsInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        List<CommitInfo> allCommits = getCommitsForRepo(login, repoFullName);
        List<PullRequestInfo> allPrs = getPullRequestsForRepo(login, repoFullName);
        List<ReviewInfo> allReviews = allPrs.stream()
                .flatMap(pr -> getPullRequestReviewsPort.getPullRequestReviews(login, repoFullName, pr.prNumber()).stream())
                .toList();
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);
        Map<String, Long> commitsByContributor = allCommits.stream()
                .filter(c -> c.authorLogin() != null)
                .collect(Collectors.groupingBy(CommitInfo::authorLogin, Collectors.counting()));
        Map<String, Long> prsByContributor = allPrs.stream()
                .filter(pr -> pr.authorLogin() != null)
                .collect(Collectors.groupingBy(PullRequestInfo::authorLogin, Collectors.counting()));
        Map<String, Long> reviewsByContributor = allReviews.stream()
                .filter(r -> r.reviewer() != null && r.reviewer().login() != null)
                .collect(Collectors.groupingBy(r -> r.reviewer().login(), Collectors.counting()));
        Set<String> active30Days = new HashSet<>();
        allCommits.stream().filter(c -> c.committedAt() != null && c.committedAt().isAfter(thirtyDaysAgo))
                .filter(c -> c.authorLogin() != null)
                .forEach(c -> active30Days.add(c.authorLogin()));
        allPrs.stream().filter(pr -> pr.createdAt() != null && pr.createdAt().isAfter(thirtyDaysAgo))
                .filter(pr -> pr.authorLogin() != null)
                .forEach(pr -> active30Days.add(pr.authorLogin()));
        allReviews.stream().filter(r -> r.submittedAt() != null && r.submittedAt().isAfter(thirtyDaysAgo))
                .filter(r -> r.reviewer() != null && r.reviewer().login() != null)
                .forEach(r -> active30Days.add(r.reviewer().login()));
        Set<String> active7Days = new HashSet<>();
        allCommits.stream().filter(c -> c.committedAt() != null && c.committedAt().isAfter(sevenDaysAgo))
                .filter(c -> c.authorLogin() != null)
                .forEach(c -> active7Days.add(c.authorLogin()));
        allPrs.stream().filter(pr -> pr.createdAt() != null && pr.createdAt().isAfter(sevenDaysAgo))
                .filter(pr -> pr.authorLogin() != null)
                .forEach(pr -> active7Days.add(pr.authorLogin()));
        allReviews.stream().filter(r -> r.submittedAt() != null && r.submittedAt().isAfter(sevenDaysAgo))
                .filter(r -> r.reviewer() != null && r.reviewer().login() != null)
                .forEach(r -> active7Days.add(r.reviewer().login()));
        return new ContributorMetricsInput(
                commitsByContributor, prsByContributor, reviewsByContributor, active30Days, active7Days
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryContributorMetricsInfo getContributorMetricsInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        List<CommitInfo> allCommits = getCommitsForRepo(login, repoFullName);
        List<PullRequestInfo> allPrs = getPullRequestsForRepo(login, repoFullName);
        List<ReviewInfo> allReviews = allPrs.stream()
            .flatMap(pr -> getPullRequestReviewsPort.getPullRequestReviews(login, repoFullName, pr.prNumber()).stream())
            .toList();

        Set<String> allContributors = new HashSet<>();
        allCommits.stream().filter(c -> c.authorLogin() != null)
            .forEach(c -> allContributors.add(c.authorLogin()));
        allPrs.stream().filter(pr -> pr.authorLogin() != null)
            .forEach(pr -> allContributors.add(pr.authorLogin()));
        allReviews.stream().filter(r -> r.reviewer() != null && r.reviewer().login() != null)
            .forEach(r -> allContributors.add(r.reviewer().login()));

        long totalContributors = allContributors.size();

        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);

        Set<String> active30Days = new HashSet<>();
        allCommits.stream().filter(c -> c.committedAt() != null && c.committedAt().isAfter(thirtyDaysAgo))
            .filter(c -> c.authorLogin() != null)
            .forEach(c -> active30Days.add(c.authorLogin()));
        allPrs.stream().filter(pr -> pr.createdAt() != null && pr.createdAt().isAfter(thirtyDaysAgo))
            .filter(pr -> pr.authorLogin() != null)
            .forEach(pr -> active30Days.add(pr.authorLogin()));
        allReviews.stream().filter(r -> r.submittedAt() != null && r.submittedAt().isAfter(thirtyDaysAgo))
            .filter(r -> r.reviewer() != null && r.reviewer().login() != null)
            .forEach(r -> active30Days.add(r.reviewer().login()));

        Set<String> active7Days = new HashSet<>();
        allCommits.stream().filter(c -> c.committedAt() != null && c.committedAt().isAfter(sevenDaysAgo))
            .filter(c -> c.authorLogin() != null)
            .forEach(c -> active7Days.add(c.authorLogin()));
        allPrs.stream().filter(pr -> pr.createdAt() != null && pr.createdAt().isAfter(sevenDaysAgo))
            .filter(pr -> pr.authorLogin() != null)
            .forEach(pr -> active7Days.add(pr.authorLogin()));
        allReviews.stream().filter(r -> r.submittedAt() != null && r.submittedAt().isAfter(sevenDaysAgo))
            .filter(r -> r.reviewer() != null && r.reviewer().login() != null)
            .forEach(r -> active7Days.add(r.reviewer().login()));

        Map<String, Long> commitsByContributor = allCommits.stream()
            .filter(c -> c.authorLogin() != null)
            .collect(Collectors.groupingBy(CommitInfo::authorLogin, Collectors.counting()));
        Map<String, Long> prsByContributor = allPrs.stream()
            .filter(pr -> pr.authorLogin() != null)
            .collect(Collectors.groupingBy(PullRequestInfo::authorLogin, Collectors.counting()));
        Map<String, Long> reviewsByContributor = allReviews.stream()
            .filter(r -> r.reviewer() != null && r.reviewer().login() != null)
            .collect(Collectors.groupingBy(r -> r.reviewer().login(), Collectors.counting()));

        List<RepositoryContributorMetricsInfo.ContributorStatsInfo> topByCommits = commitsByContributor.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String contributor = e.getKey();
                long commitCount = e.getValue();
                long prCount = prsByContributor.getOrDefault(contributor, 0L);
                long reviewCount = reviewsByContributor.getOrDefault(contributor, 0L);
                return new RepositoryContributorMetricsInfo.ContributorStatsInfo(
                    contributor, commitCount, prCount, reviewCount, commitCount + prCount + reviewCount);
            })
            .toList();

        List<RepositoryContributorMetricsInfo.ContributorStatsInfo> topByPrs = prsByContributor.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String contributor = e.getKey();
                long prCount = e.getValue();
                long commitCount = commitsByContributor.getOrDefault(contributor, 0L);
                long reviewCount = reviewsByContributor.getOrDefault(contributor, 0L);
                return new RepositoryContributorMetricsInfo.ContributorStatsInfo(
                    contributor, commitCount, prCount, reviewCount, commitCount + prCount + reviewCount);
            })
            .toList();

        List<RepositoryContributorMetricsInfo.ContributorStatsInfo> topByReviews = reviewsByContributor.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String contributor = e.getKey();
                long reviewCount = e.getValue();
                long commitCount = commitsByContributor.getOrDefault(contributor, 0L);
                long prCount = prsByContributor.getOrDefault(contributor, 0L);
                return new RepositoryContributorMetricsInfo.ContributorStatsInfo(
                    contributor, commitCount, prCount, reviewCount, commitCount + prCount + reviewCount);
            })
            .toList();

        List<Long> allContributions = new ArrayList<>();
        allContributors.forEach(contributor -> {
            long total = commitsByContributor.getOrDefault(contributor, 0L) +
                prsByContributor.getOrDefault(contributor, 0L) +
                reviewsByContributor.getOrDefault(contributor, 0L);
            allContributions.add(total);
        });
        allContributions.sort(Collections.reverseOrder());

        long totalContributionsSum = allContributions.stream().mapToLong(l -> l).sum();
        long top10Contributions = allContributions.stream().limit(10).mapToLong(l -> l).sum();
        BigDecimal top10Pct = totalContributionsSum > 0
            ? BigDecimal.valueOf(top10Contributions).divide(BigDecimal.valueOf(totalContributionsSum), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;

        long singleCommitContributors = commitsByContributor.values().stream().filter(count -> count == 1).count();
        long regularContributors = commitsByContributor.values().stream().filter(count -> count >= 5).count();

        return new RepositoryContributorMetricsInfo(
            totalContributors, (long) active30Days.size(), (long) active7Days.size(),
            topByCommits, topByPrs, topByReviews,
            commitsByContributor, prsByContributor, reviewsByContributor,
            top10Pct, singleCommitContributors, regularContributors
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryContributorMetricsInfo getContributorMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getContributorMetricsInfo(login, repoFullName);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthScoreInput getHealthScoreInput(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        ActivityTrendInput atInput = getActivityTrendInput(login, repoFullName);
        ActivityTrendResult atResult = ActivityTrendCalculator.calculate(atInput);
        OverviewInput overview = getOverviewInput(login, repoFullName);
        int stars = Optional.ofNullable(overview.stars()).orElse(0);
        int forks = Optional.ofNullable(overview.forks()).orElse(0);
        int watchers = Optional.ofNullable(overview.watchers()).orElse(0);
        RepositoryCodeQualityInfo codeQualityInfo = getCodeQualityInfo(login, repoFullName);
        BigDecimal codeQualityScore = codeQualityInfo != null && codeQualityInfo.overallQualityScore() != null
                ? codeQualityInfo.overallQualityScore() : BigDecimal.ZERO;
        ContributorMetricsInput coInput = getContributorMetricsInput(login, repoFullName);
        ContributorMetricsResult coResult = ContributorMetricsCalculator.calculate(coInput);
        return new HealthScoreInput(
                atResult.totalActivityLast30Days(),
                stars, forks, watchers,
                codeQualityScore,
                coResult.totalContributors(),
                coResult.activeContributorsLast30Days()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryHealthScoreInfo getHealthScoreInfo(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        HealthScoreInput input = getHealthScoreInput(login, repoFullName);
        HealthScoreResult result = HealthScoreCalculator.calculate(input);
        return RepositoryMetricsPersistenceMapper.toHealthScoreInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryHealthScoreInfo getHealthScore(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        return getHealthScoreInfo(login, repoFullName);
    }
}

