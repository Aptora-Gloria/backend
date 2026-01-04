package com.optimetric.metrics.infrastructure.persistence.adapter;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.metrics.api.response.*;
import com.optimetric.metrics.application.port.UserMetricsServicePort;
import com.optimetric.github.infrastructure.persistence.entity.*;
import com.optimetric.github.infrastructure.persistence.jpa.*;
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
public class UserMetricsServiceAdapter implements UserMetricsServicePort {

    private final AuthServicePort authServicePort;
    private final RepositoryRepository repositoryRepository;
    private final PullRequestRepository pullRequestRepository;
    private final PullRequestReviewRepository pullRequestReviewRepository;
    private final CommitRepository commitRepository;
    private final LanguageRepository languageRepository;

    @Override
    @Transactional(readOnly = true)
    public UserMetricsDto getUserMetrics(String login) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        List<RepositoryEntity> repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user);
        
        return new UserMetricsDto(
            getPrMergeRate(login),
            getPrSize(login),
            getReviewStats(login),
            getCommitTimeAnalysis(login),
            getLanguageDiversity(login),
            getActivityTrend(login)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PrMergeRateDto getPrMergeRate(String login) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        List<RepositoryEntity> repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user);
        
        List<PullRequestEntity> allPrs = repositories.stream()
            .flatMap(repo -> pullRequestRepository.findByRepositoryOrderByCreatedAtDesc(repo).stream())
            .toList();
        
        long total = allPrs.size();
        long merged = allPrs.stream().filter(pr -> pr.getMergedAt() != null).count();
        long open = allPrs.stream().filter(pr -> "open".equals(pr.getState())).count();
        long closed = allPrs.stream().filter(pr -> "closed".equals(pr.getState()) && pr.getMergedAt() == null).count();
        
        BigDecimal mergeRate = total > 0 
            ? BigDecimal.valueOf(merged).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        // Calculate average merge time
        List<Duration> mergeTimes = allPrs.stream()
            .filter(pr -> pr.getMergedAt() != null && pr.getCreatedAt() != null)
            .map(pr -> Duration.between(pr.getCreatedAt(), pr.getMergedAt()))
            .toList();
        
        BigDecimal avgMergeTimeHours = mergeTimes.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(mergeTimes.stream()
                .mapToLong(Duration::toHours)
                .average()
                .orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);
        
        return new PrMergeRateDto(total, merged, open, closed, mergeRate, avgMergeTimeHours);
    }

    @Override
    @Transactional(readOnly = true)
    public PrSizeDto getPrSize(String login) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        List<RepositoryEntity> repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user);
        
        List<PullRequestEntity> prsWithStats = repositories.stream()
            .flatMap(repo -> pullRequestRepository.findByRepositoryOrderByCreatedAtDesc(repo).stream())
            .filter(pr -> pr.getAdditions() != null && pr.getDeletions() != null && pr.getChangedFiles() != null)
            .toList();
        
        if (prsWithStats.isEmpty()) {
            return new PrSizeDto(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
        }
        
        BigDecimal avgAdditions = BigDecimal.valueOf(
            prsWithStats.stream().mapToInt(PullRequestEntity::getAdditions).average().orElse(0.0)
        ).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal avgDeletions = BigDecimal.valueOf(
            prsWithStats.stream().mapToInt(PullRequestEntity::getDeletions).average().orElse(0.0)
        ).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal avgChangedFiles = BigDecimal.valueOf(
            prsWithStats.stream().mapToInt(PullRequestEntity::getChangedFiles).average().orElse(0.0)
        ).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal avgTotalChanges = avgAdditions.add(avgDeletions);
        
        long small = prsWithStats.stream()
            .filter(pr -> (pr.getAdditions() + pr.getDeletions()) < 100).count();
        long medium = prsWithStats.stream()
            .filter(pr -> {
                int total = pr.getAdditions() + pr.getDeletions();
                return total >= 100 && total <= 500;
            }).count();
        long large = prsWithStats.stream()
            .filter(pr -> (pr.getAdditions() + pr.getDeletions()) > 500).count();
        
        long total = prsWithStats.size();
        BigDecimal smallPct = total > 0 
            ? BigDecimal.valueOf(small).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        BigDecimal mediumPct = total > 0
            ? BigDecimal.valueOf(medium).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        BigDecimal largePct = total > 0
            ? BigDecimal.valueOf(large).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        return new PrSizeDto(avgAdditions, avgDeletions, avgChangedFiles, avgTotalChanges,
            small, medium, large, smallPct, mediumPct, largePct);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewStatsDto getReviewStats(String login) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        List<RepositoryEntity> repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user);
        
        List<PullRequestEntity> allPrs = repositories.stream()
            .flatMap(repo -> pullRequestRepository.findByRepositoryOrderByCreatedAtDesc(repo).stream())
            .toList();
        
        List<PullRequestReviewEntity> allReviews = allPrs.stream()
            .flatMap(pr -> pullRequestReviewRepository.findByPullRequestOrderBySubmittedAtAsc(pr).stream())
            .toList();
        
        long total = allReviews.size();
        long approved = allReviews.stream().filter(r -> "APPROVED".equals(r.getState())).count();
        long changesRequested = allReviews.stream().filter(r -> "CHANGES_REQUESTED".equals(r.getState())).count();
        long commented = allReviews.stream().filter(r -> "COMMENTED".equals(r.getState())).count();
        
        BigDecimal avgReviewsPerPr = allPrs.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(total).divide(BigDecimal.valueOf(allPrs.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal approvalRate = total > 0
            ? BigDecimal.valueOf(approved).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        Map<String, Long> reviewsByState = allReviews.stream()
            .collect(Collectors.groupingBy(
                r -> r.getState() != null ? r.getState() : "UNKNOWN",
                Collectors.counting()
            ));
        
        long prsWithoutReviews = allPrs.stream()
            .filter(pr -> pullRequestReviewRepository.findByPullRequestOrderBySubmittedAtAsc(pr).isEmpty())
            .count();
        
        BigDecimal prsWithReviewsPct = allPrs.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(allPrs.size() - prsWithoutReviews)
                .divide(BigDecimal.valueOf(allPrs.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return new ReviewStatsDto(total, approved, changesRequested, commented, avgReviewsPerPr,
            approvalRate, reviewsByState, prsWithoutReviews, prsWithReviewsPct);
    }

    @Override
    @Transactional(readOnly = true)
    public CommitTimeAnalysisDto getCommitTimeAnalysis(String login) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        List<RepositoryEntity> repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user);
        
        List<CommitEntity> allCommits = repositories.stream()
            .flatMap(repo -> commitRepository.findByRepository(repo).stream())
            .filter(c -> c.getCommittedAt() != null)
            .toList();
        
        Map<Integer, Long> commitsByHour = allCommits.stream()
            .collect(Collectors.groupingBy(
                c -> c.getCommittedAt().getHour(),
                Collectors.counting()
            ));
        
        Map<String, Long> commitsByDayOfWeek = allCommits.stream()
            .collect(Collectors.groupingBy(
                c -> c.getCommittedAt().getDayOfWeek().name(),
                Collectors.counting()
            ));
        
        long weekday = allCommits.stream()
            .filter(c -> {
                DayOfWeek day = c.getCommittedAt().getDayOfWeek();
                return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
            }).count();
        long weekend = allCommits.size() - weekday;
        
        BigDecimal weekdayPct = allCommits.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(weekday).divide(BigDecimal.valueOf(allCommits.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        BigDecimal weekendPct = allCommits.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(weekend).divide(BigDecimal.valueOf(allCommits.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        Integer mostActiveHour = commitsByHour.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        String mostActiveDay = commitsByDayOfWeek.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);
        
        long last30Days = allCommits.stream()
            .filter(c -> c.getCommittedAt().isAfter(thirtyDaysAgo))
            .count();
        long last7Days = allCommits.stream()
            .filter(c -> c.getCommittedAt().isAfter(sevenDaysAgo))
            .count();
        
        return new CommitTimeAnalysisDto(commitsByHour, commitsByDayOfWeek, weekday, weekend,
            weekdayPct, weekendPct, mostActiveHour, mostActiveDay, last30Days, last7Days);
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageDiversityDto getLanguageDiversity(String login) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        List<RepositoryEntity> repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user);
        
        Map<RepositoryEntity, List<LanguageEntity>> repoLanguages = repositories.stream()
            .collect(Collectors.toMap(
                repo -> repo,
                repo -> languageRepository.findAllByRepository_FullName(repo.getFullName())
            ));
        
        Set<String> uniqueLanguages = repoLanguages.values().stream()
            .flatMap(List::stream)
            .map(LanguageEntity::getLanguageName)
            .collect(Collectors.toSet());
        
        int totalLanguages = uniqueLanguages.size();
        
        BigDecimal avgLanguagesPerRepo = repositories.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(repoLanguages.values().stream()
                .mapToInt(List::size)
                .average()
                .orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);
        
        Map<String, Long> languageUsageCount = repoLanguages.values().stream()
            .flatMap(List::stream)
            .map(LanguageEntity::getLanguageName)
            .collect(Collectors.groupingBy(
                lang -> lang,
                Collectors.counting()
            ));
        
        long totalBytes = repoLanguages.values().stream()
            .flatMap(List::stream)
            .mapToLong(LanguageEntity::getBytes)
            .sum();
        
        Map<String, BigDecimal> languagePercentageDistribution = languageUsageCount.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> totalBytes > 0
                    ? BigDecimal.valueOf(repoLanguages.values().stream()
                        .flatMap(List::stream)
                        .filter(l -> l.getLanguageName().equals(e.getKey()))
                        .mapToLong(LanguageEntity::getBytes)
                        .sum())
                        .divide(BigDecimal.valueOf(totalBytes), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO
            ));
        
        List<LanguageDiversityDto.LanguageUsageDto> topLanguages = languageUsageCount.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String lang = e.getKey();
                long repoCount = e.getValue();
                long langBytes = repoLanguages.values().stream()
                    .flatMap(List::stream)
                    .filter(l -> l.getLanguageName().equals(lang))
                    .mapToLong(LanguageEntity::getBytes)
                    .sum();
                BigDecimal percentage = totalBytes > 0
                    ? BigDecimal.valueOf(langBytes).divide(BigDecimal.valueOf(totalBytes), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
                return new LanguageDiversityDto.LanguageUsageDto(lang, repoCount, percentage, langBytes);
            })
            .toList();
        
        long singleLanguageRepos = repoLanguages.values().stream()
            .filter(langs -> langs.size() == 1)
            .count();
        long multiLanguageRepos = repoLanguages.values().stream()
            .filter(langs -> langs.size() > 1)
            .count();
        
        BigDecimal multiLanguagePct = repositories.isEmpty()
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(multiLanguageRepos).divide(BigDecimal.valueOf(repositories.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return new LanguageDiversityDto(totalLanguages, totalLanguages, avgLanguagesPerRepo,
            languageUsageCount, languagePercentageDistribution, topLanguages,
            singleLanguageRepos, multiLanguageRepos, multiLanguagePct);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityTrendDto getActivityTrend(String login) throws UserNotFoundException {
        User user = authServicePort.findByLoginOrThrow(login);
        List<RepositoryEntity> repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user);
        
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime twelveWeeksAgo = now.minusWeeks(12);
        OffsetDateTime sixMonthsAgo = now.minusMonths(6);
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        OffsetDateTime sevenDaysAgo = now.minusDays(7);
        
        List<CommitEntity> commits = repositories.stream()
            .flatMap(repo -> commitRepository.findByRepository(repo).stream())
            .filter(c -> c.getCommittedAt() != null && c.getCommittedAt().isAfter(twelveWeeksAgo))
            .toList();
        
        List<PullRequestEntity> prs = repositories.stream()
            .flatMap(repo -> pullRequestRepository.findByRepositoryOrderByCreatedAtDesc(repo).stream())
            .filter(pr -> pr.getCreatedAt() != null && pr.getCreatedAt().isAfter(twelveWeeksAgo))
            .toList();
        
        List<PullRequestReviewEntity> reviews = prs.stream()
            .flatMap(pr -> pullRequestReviewRepository.findByPullRequestOrderBySubmittedAtAsc(pr).stream())
            .filter(r -> r.getSubmittedAt() != null && r.getSubmittedAt().isAfter(twelveWeeksAgo))
            .toList();
        
        // Weekly trend (last 12 weeks)
        List<ActivityTrendDto.WeeklyActivityDto> weeklyTrend = new ArrayList<>();
        DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("yyyy-'W'ww");
        for (int i = 11; i >= 0; i--) {
            OffsetDateTime weekStart = now.minusWeeks(i).with(DayOfWeek.MONDAY);
            OffsetDateTime weekEnd = weekStart.plusDays(6);
            String week = weekStart.format(weekFormatter);
            
            long weekCommits = commits.stream()
                .filter(c -> !c.getCommittedAt().isBefore(weekStart) && !c.getCommittedAt().isAfter(weekEnd))
                .count();
            long weekPrs = prs.stream()
                .filter(pr -> !pr.getCreatedAt().isBefore(weekStart) && !pr.getCreatedAt().isAfter(weekEnd))
                .count();
            long weekReviews = reviews.stream()
                .filter(r -> !r.getSubmittedAt().isBefore(weekStart) && !r.getSubmittedAt().isAfter(weekEnd))
                .count();
            
            weeklyTrend.add(new ActivityTrendDto.WeeklyActivityDto(week, weekCommits, weekPrs, weekReviews, weekCommits + weekPrs + weekReviews));
        }
        
        // Monthly trend (last 6 months)
        List<ActivityTrendDto.MonthlyActivityDto> monthlyTrend = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            OffsetDateTime monthStart = now.minusMonths(i).withDayOfMonth(1);
            OffsetDateTime monthEnd = monthStart.plusMonths(1).minusDays(1);
            String month = monthStart.format(monthFormatter);
            
            long monthCommits = commits.stream()
                .filter(c -> !c.getCommittedAt().isBefore(monthStart) && !c.getCommittedAt().isAfter(monthEnd))
                .count();
            long monthPrs = prs.stream()
                .filter(pr -> !pr.getCreatedAt().isBefore(monthStart) && !pr.getCreatedAt().isAfter(monthEnd))
                .count();
            long monthReviews = reviews.stream()
                .filter(r -> !r.getSubmittedAt().isBefore(monthStart) && !r.getSubmittedAt().isAfter(monthEnd))
                .count();
            
            monthlyTrend.add(new ActivityTrendDto.MonthlyActivityDto(month, monthCommits, monthPrs, monthReviews, monthCommits + monthPrs + monthReviews));
        }
        
        long total30Days = commits.stream().filter(c -> c.getCommittedAt().isAfter(thirtyDaysAgo)).count() +
            prs.stream().filter(pr -> pr.getCreatedAt().isAfter(thirtyDaysAgo)).count() +
            reviews.stream().filter(r -> r.getSubmittedAt().isAfter(thirtyDaysAgo)).count();
        
        long total7Days = commits.stream().filter(c -> c.getCommittedAt().isAfter(sevenDaysAgo)).count() +
            prs.stream().filter(pr -> pr.getCreatedAt().isAfter(sevenDaysAgo)).count() +
            reviews.stream().filter(r -> r.getSubmittedAt().isAfter(sevenDaysAgo)).count();
        
        // Calculate growth rate (comparing last 30 days to previous 30 days)
        OffsetDateTime sixtyDaysAgo = now.minusDays(60);
        long previous30Days = commits.stream()
                .filter(c -> c.getCommittedAt().isAfter(sixtyDaysAgo) && c.getCommittedAt().isBefore(thirtyDaysAgo)).count() +
            prs.stream().filter(pr -> pr.getCreatedAt().isAfter(sixtyDaysAgo) && pr.getCreatedAt().isBefore(thirtyDaysAgo)).count() +
            reviews.stream().filter(r -> r.getSubmittedAt().isAfter(sixtyDaysAgo) && r.getSubmittedAt().isBefore(thirtyDaysAgo)).count();
        
        BigDecimal growthRate = previous30Days > 0
            ? BigDecimal.valueOf(total30Days - previous30Days).divide(BigDecimal.valueOf(previous30Days), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        String mostActiveWeek = weeklyTrend.stream()
            .max(Comparator.comparing(w -> w.totalActivity()))
            .map(ActivityTrendDto.WeeklyActivityDto::week)
            .orElse(null);
        
        String mostActiveMonth = monthlyTrend.stream()
            .max(Comparator.comparing(m -> m.totalActivity()))
            .map(ActivityTrendDto.MonthlyActivityDto::month)
            .orElse(null);
        
        Map<String, Long> commitsByDay = commits.stream()
            .filter(c -> c.getCommittedAt().isAfter(thirtyDaysAgo))
            .collect(Collectors.groupingBy(
                c -> c.getCommittedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                Collectors.counting()
            ));
        
        Map<String, Long> prsByDay = prs.stream()
            .filter(pr -> pr.getCreatedAt().isAfter(thirtyDaysAgo))
            .collect(Collectors.groupingBy(
                pr -> pr.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                Collectors.counting()
            ));
        
        ActivityTrendDto.ActivityBreakdownDto breakdown = new ActivityTrendDto.ActivityBreakdownDto(
            commits.stream().filter(c -> c.getCommittedAt().isAfter(thirtyDaysAgo)).count(),
            prs.stream().filter(pr -> pr.getCreatedAt().isAfter(thirtyDaysAgo)).count(),
            reviews.stream().filter(r -> r.getSubmittedAt().isAfter(thirtyDaysAgo)).count(),
            commitsByDay,
            prsByDay
        );
        
        return new ActivityTrendDto(weeklyTrend, monthlyTrend, total30Days, total7Days,
            growthRate, mostActiveWeek, mostActiveMonth, breakdown);
    }
}

