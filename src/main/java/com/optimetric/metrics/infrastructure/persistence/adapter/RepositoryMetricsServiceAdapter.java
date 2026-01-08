package com.optimetric.metrics.infrastructure.persistence.adapter;

import com.optimetric.auth.application.AuthServicePort;
import com.optimetric.github.application.port.RepoServicePort;
import com.optimetric.github.infrastructure.persistence.adapter.RepositoryPersistenceAdapter;
import com.optimetric.metrics.api.response.*;
import com.optimetric.metrics.application.port.RepositoryMetricsServicePort;
import com.optimetric.github.infrastructure.persistence.entity.*;
import com.optimetric.github.infrastructure.persistence.jpa.*;
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
public class RepositoryMetricsServiceAdapter implements RepositoryMetricsServicePort {

    private final AuthServicePort authServicePort;
    private final RepoServicePort repoServicePort;
    private final RepositoryPersistenceAdapter repositoryPersistenceAdapter;
    private final PullRequestRepository pullRequestRepository;
    private final PullRequestReviewRepository pullRequestReviewRepository;
    private final CommitRepository commitRepository;
    private final LanguageRepository languageRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional(readOnly = true)
    public RepositoryMetricsDto getRepositoryMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        return new RepositoryMetricsDto(
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

    /**
     * Helper method to get RepositoryEntity from RepositoryInfo.
     * This is needed because infrastructure adapters need entities for JPA operations.
     */
    private RepositoryEntity getRepositoryEntity(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        com.optimetric.github.application.dto.RepositoryInfo repoInfo = repoServicePort.getByFullNameForCurrentUserOrThrow(login, repoFullName);
        return repositoryPersistenceAdapter.findEntityById(repoInfo.id())
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with id: " + repoInfo.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryOverviewDto getOverview(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = getRepositoryEntity(login, repoFullName);
        
        OffsetDateTime now = OffsetDateTime.now();
        long ageDays = repository.getCreatedAt() != null 
            ? Duration.between(repository.getCreatedAt(), now).toDays()
            : 0;
        
        boolean isActive = repository.getPushedAt() != null 
            && repository.getPushedAt().isAfter(now.minusDays(30));
        
        long daysSinceLastPush = repository.getPushedAt() != null
            ? Duration.between(repository.getPushedAt(), now).toDays()
            : 0;
        
        return new RepositoryOverviewDto(
            repository.getFullName(),
            repository.getName(),
            repository.getDescription(),
            repository.getHtmlUrl(),
            repository.getOwnerLogin(),
            repository.getOwnerType() != null ? repository.getOwnerType().name() : null,
            repository.getDefaultBranch(),
            repository.getLanguage(),
            repository.getStargazersCount(),
            repository.getForksCount(),
            repository.getWatchersCount(),
            repository.getOpenIssuesCount(),
            repository.getIsPrivate(),
            repository.getIsFork(),
            repository.getSize(),
            repository.getCreatedAt(),
            repository.getUpdatedAt(),
            repository.getPushedAt(),
            repository.getLastSyncedAt(),
            ageDays,
            isActive,
            daysSinceLastPush
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryPrMetricsDto getPullRequestMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = getRepositoryEntity(login, repoFullName);
        List<PullRequestEntity> allPrs = pullRequestRepository.findByRepositoryOrderByCreatedAtDesc(repository);
        
        long total = allPrs.size();
        long open = allPrs.stream().filter(pr -> "open".equals(pr.getState())).count();
        long closed = allPrs.stream().filter(pr -> "closed".equals(pr.getState()) && pr.getMergedAt() == null).count();
        long merged = allPrs.stream().filter(pr -> pr.getMergedAt() != null).count();
        long draft = allPrs.stream().filter(pr -> Boolean.TRUE.equals(pr.getDraft())).count();
        
        BigDecimal mergeRate = total > 0
            ? BigDecimal.valueOf(merged).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        List<PullRequestEntity> prsWithStats = allPrs.stream()
            .filter(pr -> pr.getAdditions() != null && pr.getDeletions() != null && pr.getChangedFiles() != null)
            .toList();
        
        BigDecimal avgAdditions = prsWithStats.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(prsWithStats.stream().mapToInt(PullRequestEntity::getAdditions).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal avgDeletions = prsWithStats.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(prsWithStats.stream().mapToInt(PullRequestEntity::getDeletions).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal avgChangedFiles = prsWithStats.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(prsWithStats.stream().mapToInt(PullRequestEntity::getChangedFiles).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal avgTotalChanges = avgAdditions.add(avgDeletions);
        
        List<Long> prSizes = prsWithStats.stream()
            .map(pr -> (long)(pr.getAdditions() + pr.getDeletions()))
            .toList();
        long largestPrSize = prSizes.isEmpty() ? 0 : Collections.max(prSizes);
        long smallestPrSize = prSizes.isEmpty() ? 0 : Collections.min(prSizes);
        
        long small = prsWithStats.stream().filter(pr -> (pr.getAdditions() + pr.getDeletions()) < 100).count();
        long medium = prsWithStats.stream().filter(pr -> {
            int prTotal = pr.getAdditions() + pr.getDeletions();
            return prTotal >= 100 && prTotal <= 500;
        }).count();
        long large = prsWithStats.stream().filter(pr -> (pr.getAdditions() + pr.getDeletions()) > 500).count();
        
        long prsWithStatsCount = prsWithStats.size();
        BigDecimal smallPct = prsWithStatsCount > 0
            ? BigDecimal.valueOf(small).divide(BigDecimal.valueOf(prsWithStatsCount), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        BigDecimal mediumPct = prsWithStatsCount > 0
            ? BigDecimal.valueOf(medium).divide(BigDecimal.valueOf(prsWithStatsCount), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        BigDecimal largePct = prsWithStatsCount > 0
            ? BigDecimal.valueOf(large).divide(BigDecimal.valueOf(prsWithStatsCount), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);
        long prsLast30Days = allPrs.stream().filter(pr -> 
            pr.getCreatedAt() != null && pr.getCreatedAt().isAfter(thirtyDaysAgo)).count();
        long prsLast7Days = allPrs.stream().filter(pr -> 
            pr.getCreatedAt() != null && pr.getCreatedAt().isAfter(sevenDaysAgo)).count();
        long mergedLast30Days = allPrs.stream().filter(pr -> 
            pr.getMergedAt() != null && pr.getMergedAt().isAfter(thirtyDaysAgo)).count();
        
        List<Duration> mergeTimes = allPrs.stream()
            .filter(pr -> pr.getMergedAt() != null && pr.getCreatedAt() != null)
            .map(pr -> Duration.between(pr.getCreatedAt(), pr.getMergedAt()))
            .toList();
        BigDecimal avgMergeTimeHours = mergeTimes.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(mergeTimes.stream().mapToLong(Duration::toHours).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);
        
        Map<String, Long> prsByAuthor = allPrs.stream()
            .filter(pr -> pr.getAuthorLogin() != null)
            .collect(Collectors.groupingBy(PullRequestEntity::getAuthorLogin, Collectors.counting()));
        
        long uniqueAuthors = prsByAuthor.size();
        
        List<RepositoryPrMetricsDto.AuthorStatsDto> topAuthors = prsByAuthor.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String author = e.getKey();
                long authorPrCount = e.getValue();
                long authorMerged = allPrs.stream()
                    .filter(pr -> author.equals(pr.getAuthorLogin()) && pr.getMergedAt() != null)
                    .count();
                BigDecimal authorMergeRate = authorPrCount > 0
                    ? BigDecimal.valueOf(authorMerged).divide(BigDecimal.valueOf(authorPrCount), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
                return new RepositoryPrMetricsDto.AuthorStatsDto(author, authorPrCount, authorMerged, authorMergeRate);
            })
            .toList();
        
        return new RepositoryPrMetricsDto(
            total, open, closed, merged, draft, mergeRate,
            avgAdditions, avgDeletions, avgChangedFiles, avgTotalChanges,
            largestPrSize, smallestPrSize,
            small, medium, large, smallPct, mediumPct, largePct,
            prsLast30Days, prsLast7Days, mergedLast30Days, avgMergeTimeHours,
            uniqueAuthors, topAuthors, prsByAuthor
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryCommitMetricsDto getCommitMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = getRepositoryEntity(login, repoFullName);
        List<CommitEntity> allCommits = commitRepository.findByRepository(repository);
        
        long total = allCommits.size();
        long mergeCommits = allCommits.stream().filter(c -> Boolean.TRUE.equals(c.getMergeCommit())).count();
        long regularCommits = total - mergeCommits;
        BigDecimal mergeCommitPercentage = total > 0
            ? BigDecimal.valueOf(mergeCommits).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);
        List<CommitEntity> commitsWithDate = allCommits.stream()
            .filter(c -> c.getCommittedAt() != null)
            .toList();
        
        long commitsLast30Days = commitsWithDate.stream()
            .filter(c -> c.getCommittedAt().isAfter(thirtyDaysAgo))
            .count();
        long commitsLast7Days = commitsWithDate.stream()
            .filter(c -> c.getCommittedAt().isAfter(sevenDaysAgo))
            .count();
        
        OffsetDateTime lastCommitDate = commitsWithDate.stream()
            .map(CommitEntity::getCommittedAt)
            .max(OffsetDateTime::compareTo)
            .orElse(null);
        
        Map<Integer, Long> commitsByHour = commitsWithDate.stream()
            .collect(Collectors.groupingBy(c -> c.getCommittedAt().getHour(), Collectors.counting()));
        Map<String, Long> commitsByDayOfWeek = commitsWithDate.stream()
            .collect(Collectors.groupingBy(c -> c.getCommittedAt().getDayOfWeek().name(), Collectors.counting()));
        
        long weekday = commitsWithDate.stream()
            .filter(c -> {
                DayOfWeek day = c.getCommittedAt().getDayOfWeek();
                return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
            }).count();
        long weekend = commitsWithDate.size() - weekday;
        
        BigDecimal weekdayPct = commitsWithDate.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(weekday).divide(BigDecimal.valueOf(commitsWithDate.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        BigDecimal weekendPct = commitsWithDate.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(weekend).divide(BigDecimal.valueOf(commitsWithDate.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        String mostActiveDay = commitsByDayOfWeek.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        Integer mostActiveHour = commitsByHour.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        Map<String, Long> commitsByAuthor = allCommits.stream()
            .filter(c -> c.getAuthorLogin() != null)
            .collect(Collectors.groupingBy(CommitEntity::getAuthorLogin, Collectors.counting()));
        
        long uniqueAuthors = commitsByAuthor.size();
        
        List<RepositoryCommitMetricsDto.AuthorStatsDto> topAuthors = commitsByAuthor.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String author = e.getKey();
                long commitCount = e.getValue();
                long mergeCommitCount = allCommits.stream()
                    .filter(c -> author.equals(c.getAuthorLogin()) && Boolean.TRUE.equals(c.getMergeCommit()))
                    .count();
                return new RepositoryCommitMetricsDto.AuthorStatsDto(author, commitCount, mergeCommitCount);
            })
            .toList();
        
        List<Integer> messageLengths = allCommits.stream()
            .filter(c -> c.getMessage() != null)
            .map(c -> c.getMessage().length())
            .toList();
        BigDecimal avgMessageLength = messageLengths.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(messageLengths.stream().mapToInt(i -> i).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);
        long longestMessage = messageLengths.isEmpty() ? 0 : Collections.max(messageLengths);
        long shortestMessage = messageLengths.isEmpty() ? 0 : Collections.min(messageLengths);
        
        return new RepositoryCommitMetricsDto(
            total, mergeCommits, regularCommits, mergeCommitPercentage,
            commitsLast30Days, commitsLast7Days, lastCommitDate, mostActiveDay, mostActiveHour,
            commitsByHour, commitsByDayOfWeek, weekday, weekend, weekdayPct, weekendPct,
            uniqueAuthors, topAuthors, commitsByAuthor,
            avgMessageLength, longestMessage, shortestMessage
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryReviewMetricsDto getReviewMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = getRepositoryEntity(login, repoFullName);
        List<PullRequestEntity> allPrs = pullRequestRepository.findByRepositoryOrderByCreatedAtDesc(repository);
        
        List<PullRequestReviewEntity> allReviews = allPrs.stream()
            .flatMap(pr -> pullRequestReviewRepository.findByPullRequestOrderBySubmittedAtAsc(pr).stream())
            .toList();
        
        long total = allReviews.size();
        long approved = allReviews.stream().filter(r -> "APPROVED".equals(r.getState())).count();
        long changesRequested = allReviews.stream().filter(r -> "CHANGES_REQUESTED".equals(r.getState())).count();
        long commented = allReviews.stream().filter(r -> "COMMENTED".equals(r.getState())).count();
        
        Map<String, Long> reviewsByState = allReviews.stream()
            .collect(Collectors.groupingBy(
                r -> r.getState() != null ? r.getState() : "UNKNOWN",
                Collectors.counting()
            ));
        
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);
        long reviewsLast30Days = allReviews.stream()
            .filter(r -> r.getSubmittedAt() != null && r.getSubmittedAt().isAfter(thirtyDaysAgo))
            .count();
        long reviewsLast7Days = allReviews.stream()
            .filter(r -> r.getSubmittedAt() != null && r.getSubmittedAt().isAfter(sevenDaysAgo))
            .count();
        
        BigDecimal avgReviewsPerPr = allPrs.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(total).divide(BigDecimal.valueOf(allPrs.size()), 2, RoundingMode.HALF_UP);
        
        long prsWithoutReviews = allPrs.stream()
            .filter(pr -> pullRequestReviewRepository.findByPullRequestOrderBySubmittedAtAsc(pr).isEmpty())
            .count();
        
        BigDecimal prsWithReviewsPct = allPrs.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(allPrs.size() - prsWithoutReviews)
                .divide(BigDecimal.valueOf(allPrs.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        BigDecimal approvalRate = total > 0
            ? BigDecimal.valueOf(approved).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        Map<String, Long> reviewsByReviewer = allReviews.stream()
            .filter(r -> r.getReviewerLogin() != null)
            .collect(Collectors.groupingBy(PullRequestReviewEntity::getReviewerLogin, Collectors.counting()));
        
        Map<String, Long> approvalsByReviewer = allReviews.stream()
            .filter(r -> r.getReviewerLogin() != null && "APPROVED".equals(r.getState()))
            .collect(Collectors.groupingBy(PullRequestReviewEntity::getReviewerLogin, Collectors.counting()));
        
        long uniqueReviewers = reviewsByReviewer.size();
        
        List<RepositoryReviewMetricsDto.ReviewerStatsDto> topReviewers = reviewsByReviewer.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String reviewer = e.getKey();
                long reviewCount = e.getValue();
                long approvalCount = approvalsByReviewer.getOrDefault(reviewer, 0L);
                BigDecimal reviewerApprovalRate = reviewCount > 0
                    ? BigDecimal.valueOf(approvalCount).divide(BigDecimal.valueOf(reviewCount), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
                return new RepositoryReviewMetricsDto.ReviewerStatsDto(reviewer, reviewCount, approvalCount, reviewerApprovalRate);
            })
            .toList();
        
        return new RepositoryReviewMetricsDto(
            total, approved, changesRequested, commented, reviewsByState,
            reviewsLast30Days, reviewsLast7Days, avgReviewsPerPr, prsWithoutReviews, prsWithReviewsPct, approvalRate,
            uniqueReviewers, topReviewers, reviewsByReviewer, approvalsByReviewer
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryBranchMetricsDto getBranchMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = getRepositoryEntity(login, repoFullName);
        List<BranchEntity> allBranches = branchRepository.findByRepository(repository);
        
        long totalBranches = allBranches.size();
        String defaultBranch = repository.getDefaultBranch();
        
        Map<String, Long> commitsByBranch = allBranches.stream()
            .collect(Collectors.toMap(
                BranchEntity::getName,
                branch -> (long) commitRepository.findByBranch(branch).size()
            ));
        
        List<RepositoryBranchMetricsDto.BranchStatsDto> topBranchesByCommits = commitsByBranch.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String branchName = e.getKey();
                long commitCount = e.getValue();
                BranchEntity branch = allBranches.stream()
                    .filter(b -> b.getName().equals(branchName))
                    .findFirst()
                    .orElse(null);
                
                OffsetDateTime lastCommitDate = null;
                if (branch != null) {
                    List<CommitEntity> branchCommits = commitRepository.findByBranch(branch);
                    lastCommitDate = branchCommits.stream()
                        .filter(c -> c.getCommittedAt() != null)
                        .map(CommitEntity::getCommittedAt)
                        .max(OffsetDateTime::compareTo)
                        .orElse(null);
                }
                
                boolean isDefault = branchName.equals(defaultBranch);
                return new RepositoryBranchMetricsDto.BranchStatsDto(branchName, commitCount, lastCommitDate, isDefault);
            })
            .toList();
        
        long branchesWithNoCommits = commitsByBranch.values().stream().filter(count -> count == 0).count();
        BigDecimal avgCommitsPerBranch = totalBranches > 0
            ? BigDecimal.valueOf(commitsByBranch.values().stream().mapToLong(l -> l).sum())
                .divide(BigDecimal.valueOf(totalBranches), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        return new RepositoryBranchMetricsDto(
            totalBranches, defaultBranch, topBranchesByCommits, commitsByBranch,
            branchesWithNoCommits, avgCommitsPerBranch
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryLanguageMetricsDto getLanguageMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = getRepositoryEntity(login, repoFullName);
        List<LanguageEntity> languages = languageRepository.findAllByRepository_FullName(repoFullName);
        
        int totalLanguages = languages.size();
        
        if (languages.isEmpty()) {
            return new RepositoryLanguageMetricsDto(
                0, null, BigDecimal.ZERO, Map.of(), Map.of(), List.of(), false, BigDecimal.ZERO
            );
        }
        
        String primaryLanguage = languages.stream()
            .max(Comparator.comparing(LanguageEntity::getBytes))
            .map(LanguageEntity::getLanguageName)
            .orElse(null);
        
        long totalBytes = languages.stream().mapToLong(LanguageEntity::getBytes).sum();
        
        BigDecimal primaryLanguagePercentage = totalBytes > 0 && primaryLanguage != null
            ? BigDecimal.valueOf(languages.stream()
                .filter(l -> l.getLanguageName().equals(primaryLanguage))
                .mapToLong(LanguageEntity::getBytes)
                .sum())
                .divide(BigDecimal.valueOf(totalBytes), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        Map<String, BigDecimal> languageDistribution = languages.stream()
            .collect(Collectors.toMap(
                LanguageEntity::getLanguageName,
                l -> totalBytes > 0
                    ? BigDecimal.valueOf(l.getBytes()).divide(BigDecimal.valueOf(totalBytes), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO
            ));
        
        Map<String, Long> languageBytes = languages.stream()
            .collect(Collectors.toMap(LanguageEntity::getLanguageName, LanguageEntity::getBytes));
        
        List<RepositoryLanguageMetricsDto.LanguageStatsDto> topLanguages = languages.stream()
            .sorted(Comparator.comparing(LanguageEntity::getBytes).reversed())
            .limit(10)
            .map(l -> {
                BigDecimal percentage = totalBytes > 0
                    ? BigDecimal.valueOf(l.getBytes()).divide(BigDecimal.valueOf(totalBytes), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
                return new RepositoryLanguageMetricsDto.LanguageStatsDto(l.getLanguageName(), l.getBytes(), percentage);
            })
            .toList();
        
        boolean isMultiLanguage = totalLanguages >= 2;
        
        // Language diversity score: more languages = higher score (max 100)
        BigDecimal diversityScore = totalLanguages > 0
            ? BigDecimal.valueOf(Math.min(totalLanguages * 10, 100))
            : BigDecimal.ZERO;
        
        return new RepositoryLanguageMetricsDto(
            totalLanguages, primaryLanguage, primaryLanguagePercentage,
            languageDistribution, languageBytes, topLanguages, isMultiLanguage, diversityScore
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryActivityTrendDto getActivityTrend(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = getRepositoryEntity(login, repoFullName);
        
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime twelveWeeksAgo = now.minusWeeks(12);
        OffsetDateTime sixMonthsAgo = now.minusMonths(6);
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);
        OffsetDateTime sevenDaysAgo = now.minusDays(7);
        
        List<CommitEntity> commits = commitRepository.findByRepository(repository).stream()
            .filter(c -> c.getCommittedAt() != null && c.getCommittedAt().isAfter(twelveWeeksAgo))
            .toList();
        
        List<PullRequestEntity> prs = pullRequestRepository.findByRepositoryOrderByCreatedAtDesc(repository).stream()
            .filter(pr -> pr.getCreatedAt() != null && pr.getCreatedAt().isAfter(twelveWeeksAgo))
            .toList();
        
        List<PullRequestReviewEntity> reviews = prs.stream()
            .flatMap(pr -> pullRequestReviewRepository.findByPullRequestOrderBySubmittedAtAsc(pr).stream())
            .filter(r -> r.getSubmittedAt() != null && r.getSubmittedAt().isAfter(twelveWeeksAgo))
            .toList();
        
        // Weekly trend
        List<RepositoryActivityTrendDto.WeeklyActivityDto> weeklyTrend = new ArrayList<>();
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
            
            weeklyTrend.add(new RepositoryActivityTrendDto.WeeklyActivityDto(
                week, weekCommits, weekPrs, weekReviews, weekCommits + weekPrs + weekReviews));
        }
        
        // Monthly trend
        List<RepositoryActivityTrendDto.MonthlyActivityDto> monthlyTrend = new ArrayList<>();
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
            
            monthlyTrend.add(new RepositoryActivityTrendDto.MonthlyActivityDto(
                month, monthCommits, monthPrs, monthReviews, monthCommits + monthPrs + monthReviews));
        }
        
        long total30Days = commits.stream().filter(c -> c.getCommittedAt().isAfter(thirtyDaysAgo)).count() +
            prs.stream().filter(pr -> pr.getCreatedAt().isAfter(thirtyDaysAgo)).count() +
            reviews.stream().filter(r -> r.getSubmittedAt().isAfter(thirtyDaysAgo)).count();
        
        long total7Days = commits.stream().filter(c -> c.getCommittedAt().isAfter(sevenDaysAgo)).count() +
            prs.stream().filter(pr -> pr.getCreatedAt().isAfter(sevenDaysAgo)).count() +
            reviews.stream().filter(r -> r.getSubmittedAt().isAfter(sevenDaysAgo)).count();
        
        // Growth rate
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
            .map(RepositoryActivityTrendDto.WeeklyActivityDto::week)
            .orElse(null);
        
        String mostActiveMonth = monthlyTrend.stream()
            .max(Comparator.comparing(m -> m.totalActivity()))
            .map(RepositoryActivityTrendDto.MonthlyActivityDto::month)
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
        
        Map<String, Long> reviewsByDay = reviews.stream()
            .filter(r -> r.getSubmittedAt().isAfter(thirtyDaysAgo))
            .collect(Collectors.groupingBy(
                r -> r.getSubmittedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                Collectors.counting()
            ));
        
        RepositoryActivityTrendDto.ActivityBreakdownDto breakdown = new RepositoryActivityTrendDto.ActivityBreakdownDto(
            commits.stream().filter(c -> c.getCommittedAt().isAfter(thirtyDaysAgo)).count(),
            prs.stream().filter(pr -> pr.getCreatedAt().isAfter(thirtyDaysAgo)).count(),
            reviews.stream().filter(r -> r.getSubmittedAt().isAfter(thirtyDaysAgo)).count()
        );
        
        return new RepositoryActivityTrendDto(
            weeklyTrend, monthlyTrend, total30Days, total7Days, breakdown,
            growthRate, mostActiveWeek, mostActiveMonth, commitsByDay, prsByDay, reviewsByDay
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryCodeQualityDto getCodeQuality(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = getRepositoryEntity(login, repoFullName);
        List<PullRequestEntity> allPrs = pullRequestRepository.findByRepositoryOrderByCreatedAtDesc(repository);
        List<CommitEntity> allCommits = commitRepository.findByRepository(repository);
        
        // PR Quality
        List<PullRequestEntity> prsWithStats = allPrs.stream()
            .filter(pr -> pr.getAdditions() != null && pr.getDeletions() != null)
            .toList();
        
        BigDecimal avgPrSize = prsWithStats.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(prsWithStats.stream()
                .mapToInt(pr -> pr.getAdditions() + pr.getDeletions())
                .average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);
        
        long merged = allPrs.stream().filter(pr -> pr.getMergedAt() != null).count();
        BigDecimal prMergeRate = allPrs.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(merged).divide(BigDecimal.valueOf(allPrs.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        long totalReviews = allPrs.stream()
            .mapToLong(pr -> pullRequestReviewRepository.findByPullRequestOrderBySubmittedAtAsc(pr).size())
            .sum();
        BigDecimal avgReviewsPerPr = allPrs.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(totalReviews).divide(BigDecimal.valueOf(allPrs.size()), 2, RoundingMode.HALF_UP);
        
        long prsWithoutReviews = allPrs.stream()
            .filter(pr -> pullRequestReviewRepository.findByPullRequestOrderBySubmittedAtAsc(pr).isEmpty())
            .count();
        BigDecimal prsWithoutReviewsPct = allPrs.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(prsWithoutReviews).divide(BigDecimal.valueOf(allPrs.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        long smallPrs = prsWithStats.stream().filter(pr -> (pr.getAdditions() + pr.getDeletions()) < 100).count();
        BigDecimal smallPrPct = prsWithStats.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(smallPrs).divide(BigDecimal.valueOf(prsWithStats.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        // Commit Quality
        long mergeCommits = allCommits.stream().filter(c -> Boolean.TRUE.equals(c.getMergeCommit())).count();
        BigDecimal mergeCommitPct = allCommits.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(mergeCommits).divide(BigDecimal.valueOf(allCommits.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        List<Integer> messageLengths = allCommits.stream()
            .filter(c -> c.getMessage() != null)
            .map(c -> c.getMessage().length())
            .toList();
        BigDecimal avgMessageLength = messageLengths.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(messageLengths.stream().mapToInt(i -> i).average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal regularCommitPct = allCommits.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(allCommits.size() - mergeCommits)
                .divide(BigDecimal.valueOf(allCommits.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        // Code Change metrics
        long totalAdditions = prsWithStats.stream().mapToInt(PullRequestEntity::getAdditions).sum();
        long totalDeletions = prsWithStats.stream().mapToInt(PullRequestEntity::getDeletions).sum();
        long netCodeChange = totalAdditions - totalDeletions;
        
        BigDecimal codeGrowthRate = (totalAdditions + totalDeletions) > 0
            ? BigDecimal.valueOf(netCodeChange).divide(BigDecimal.valueOf(totalAdditions + totalDeletions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        BigDecimal avgFilesChanged = prsWithStats.stream()
            .filter(pr -> pr.getChangedFiles() != null)
            .mapToInt(PullRequestEntity::getChangedFiles)
            .average()
            .stream()
            .mapToObj(avg -> BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
            .findFirst()
            .orElse(BigDecimal.ZERO);
        
        // Overall Quality Score (weighted average)
        BigDecimal qualityScore = BigDecimal.ZERO;
        if (!allPrs.isEmpty() || !allCommits.isEmpty()) {
            // PR quality (40%), Commit quality (30%), Code change (30%)
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
        
        return new RepositoryCodeQualityDto(
            avgPrSize, prMergeRate, avgReviewsPerPr, prsWithoutReviewsPct, smallPrPct,
            mergeCommitPct, avgMessageLength, regularCommitPct,
            netCodeChange, codeGrowthRate, avgFilesChanged,
            qualityScore
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryContributorMetricsDto getContributorMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = getRepositoryEntity(login, repoFullName);
        
        List<CommitEntity> allCommits = commitRepository.findByRepository(repository);
        List<PullRequestEntity> allPrs = pullRequestRepository.findByRepositoryOrderByCreatedAtDesc(repository);
        List<PullRequestReviewEntity> allReviews = allPrs.stream()
            .flatMap(pr -> pullRequestReviewRepository.findByPullRequestOrderBySubmittedAtAsc(pr).stream())
            .toList();
        
        // Collect all contributors
        Set<String> allContributors = new HashSet<>();
        allCommits.stream().filter(c -> c.getAuthorLogin() != null)
            .forEach(c -> allContributors.add(c.getAuthorLogin()));
        allPrs.stream().filter(pr -> pr.getAuthorLogin() != null)
            .forEach(pr -> allContributors.add(pr.getAuthorLogin()));
        allReviews.stream().filter(r -> r.getReviewerLogin() != null)
            .forEach(r -> allContributors.add(r.getReviewerLogin()));
        
        long totalContributors = allContributors.size();
        
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);
        
        Set<String> active30Days = new HashSet<>();
        allCommits.stream().filter(c -> c.getCommittedAt() != null && c.getCommittedAt().isAfter(thirtyDaysAgo))
            .filter(c -> c.getAuthorLogin() != null)
            .forEach(c -> active30Days.add(c.getAuthorLogin()));
        allPrs.stream().filter(pr -> pr.getCreatedAt() != null && pr.getCreatedAt().isAfter(thirtyDaysAgo))
            .filter(pr -> pr.getAuthorLogin() != null)
            .forEach(pr -> active30Days.add(pr.getAuthorLogin()));
        allReviews.stream().filter(r -> r.getSubmittedAt() != null && r.getSubmittedAt().isAfter(thirtyDaysAgo))
            .filter(r -> r.getReviewerLogin() != null)
            .forEach(r -> active30Days.add(r.getReviewerLogin()));
        
        Set<String> active7Days = new HashSet<>();
        allCommits.stream().filter(c -> c.getCommittedAt() != null && c.getCommittedAt().isAfter(sevenDaysAgo))
            .filter(c -> c.getAuthorLogin() != null)
            .forEach(c -> active7Days.add(c.getAuthorLogin()));
        allPrs.stream().filter(pr -> pr.getCreatedAt() != null && pr.getCreatedAt().isAfter(sevenDaysAgo))
            .filter(pr -> pr.getAuthorLogin() != null)
            .forEach(pr -> active7Days.add(pr.getAuthorLogin()));
        allReviews.stream().filter(r -> r.getSubmittedAt() != null && r.getSubmittedAt().isAfter(sevenDaysAgo))
            .filter(r -> r.getReviewerLogin() != null)
            .forEach(r -> active7Days.add(r.getReviewerLogin()));
        
        Map<String, Long> commitsByContributor = allCommits.stream()
            .filter(c -> c.getAuthorLogin() != null)
            .collect(Collectors.groupingBy(CommitEntity::getAuthorLogin, Collectors.counting()));
        
        Map<String, Long> prsByContributor = allPrs.stream()
            .filter(pr -> pr.getAuthorLogin() != null)
            .collect(Collectors.groupingBy(PullRequestEntity::getAuthorLogin, Collectors.counting()));
        
        Map<String, Long> reviewsByContributor = allReviews.stream()
            .filter(r -> r.getReviewerLogin() != null)
            .collect(Collectors.groupingBy(PullRequestReviewEntity::getReviewerLogin, Collectors.counting()));
        
        // Top contributors by commits
        List<RepositoryContributorMetricsDto.ContributorStatsDto> topByCommits = commitsByContributor.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String contributor = e.getKey();
                long commitCount = e.getValue();
                long prCount = prsByContributor.getOrDefault(contributor, 0L);
                long reviewCount = reviewsByContributor.getOrDefault(contributor, 0L);
                return new RepositoryContributorMetricsDto.ContributorStatsDto(
                    contributor, commitCount, prCount, reviewCount, commitCount + prCount + reviewCount);
            })
            .toList();
        
        // Top contributors by PRs
        List<RepositoryContributorMetricsDto.ContributorStatsDto> topByPrs = prsByContributor.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String contributor = e.getKey();
                long prCount = e.getValue();
                long commitCount = commitsByContributor.getOrDefault(contributor, 0L);
                long reviewCount = reviewsByContributor.getOrDefault(contributor, 0L);
                return new RepositoryContributorMetricsDto.ContributorStatsDto(
                    contributor, commitCount, prCount, reviewCount, commitCount + prCount + reviewCount);
            })
            .toList();
        
        // Top contributors by reviews
        List<RepositoryContributorMetricsDto.ContributorStatsDto> topByReviews = reviewsByContributor.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> {
                String contributor = e.getKey();
                long reviewCount = e.getValue();
                long commitCount = commitsByContributor.getOrDefault(contributor, 0L);
                long prCount = prsByContributor.getOrDefault(contributor, 0L);
                return new RepositoryContributorMetricsDto.ContributorStatsDto(
                    contributor, commitCount, prCount, reviewCount, commitCount + prCount + reviewCount);
            })
            .toList();
        
        // Pareto analysis
        List<Long> allContributions = new ArrayList<>();
        allContributors.forEach(contributor -> {
            long total = commitsByContributor.getOrDefault(contributor, 0L) +
                prsByContributor.getOrDefault(contributor, 0L) +
                reviewsByContributor.getOrDefault(contributor, 0L);
            allContributions.add(total);
        });
        allContributions.sort(Collections.reverseOrder());
        
        long totalContributions = allContributions.stream().mapToLong(l -> l).sum();
        long top10Contributions = allContributions.stream().limit(10).mapToLong(l -> l).sum();
        BigDecimal top10Pct = totalContributions > 0
            ? BigDecimal.valueOf(top10Contributions).divide(BigDecimal.valueOf(totalContributions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        long singleCommitContributors = commitsByContributor.values().stream().filter(count -> count == 1).count();
        long regularContributors = commitsByContributor.values().stream().filter(count -> count >= 5).count();
        
        return new RepositoryContributorMetricsDto(
            totalContributors, (long) active30Days.size(), (long) active7Days.size(),
            topByCommits, topByPrs, topByReviews,
            commitsByContributor, prsByContributor, reviewsByContributor,
            top10Pct, singleCommitContributors, regularContributors
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryHealthScoreDto getHealthScore(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryEntity repository = getRepositoryEntity(login, repoFullName);
        
        // Get all metrics
        RepositoryActivityTrendDto activityTrend = getActivityTrend(login, repoFullName);
        RepositoryCodeQualityDto codeQuality = getCodeQuality(login, repoFullName);
        RepositoryContributorMetricsDto contributors = getContributorMetrics(login, repoFullName);
        
        // Activity Score (0-100)
        BigDecimal activityScore = BigDecimal.ZERO;
        if (activityTrend.totalActivityLast30Days() > 0) {
            // Based on recent activity (commits + PRs + reviews)
            long activity = activityTrend.totalActivityLast30Days();
            if (activity >= 100) activityScore = BigDecimal.valueOf(100);
            else if (activity >= 50) activityScore = BigDecimal.valueOf(80);
            else if (activity >= 20) activityScore = BigDecimal.valueOf(60);
            else if (activity >= 10) activityScore = BigDecimal.valueOf(40);
            else activityScore = BigDecimal.valueOf(20);
        }
        
        // Popularity Score (0-100)
        BigDecimal popularityScore = BigDecimal.ZERO;
        int stars = repository.getStargazersCount() != null ? repository.getStargazersCount() : 0;
        int forks = repository.getForksCount() != null ? repository.getForksCount() : 0;
        int watchers = repository.getWatchersCount() != null ? repository.getWatchersCount() : 0;
        long popularity = stars + forks + watchers;
        if (popularity >= 1000) popularityScore = BigDecimal.valueOf(100);
        else if (popularity >= 500) popularityScore = BigDecimal.valueOf(80);
        else if (popularity >= 100) popularityScore = BigDecimal.valueOf(60);
        else if (popularity >= 50) popularityScore = BigDecimal.valueOf(40);
        else if (popularity >= 10) popularityScore = BigDecimal.valueOf(20);
        else popularityScore = BigDecimal.valueOf(10);
        
        // Code Quality Score (from codeQuality metric)
        BigDecimal codeQualityScore = codeQuality.overallQualityScore();
        
        // Community Score (0-100)
        BigDecimal communityScore = BigDecimal.ZERO;
        long totalContributors = contributors.totalContributors();
        long activeContributors = contributors.activeContributorsLast30Days();
        if (totalContributors >= 10 && activeContributors >= 5) communityScore = BigDecimal.valueOf(100);
        else if (totalContributors >= 5 && activeContributors >= 3) communityScore = BigDecimal.valueOf(80);
        else if (totalContributors >= 3 && activeContributors >= 2) communityScore = BigDecimal.valueOf(60);
        else if (totalContributors >= 2) communityScore = BigDecimal.valueOf(40);
        else if (totalContributors >= 1) communityScore = BigDecimal.valueOf(20);
        else communityScore = BigDecimal.ZERO;
        
        // Overall Health Score (weighted average)
        BigDecimal overallHealthScore = activityScore.multiply(BigDecimal.valueOf(0.3))
            .add(popularityScore.multiply(BigDecimal.valueOf(0.2)))
            .add(codeQualityScore.multiply(BigDecimal.valueOf(0.3)))
            .add(communityScore.multiply(BigDecimal.valueOf(0.2)))
            .setScale(2, RoundingMode.HALF_UP);
        
        // Health Status
        String healthStatus;
        if (overallHealthScore.compareTo(BigDecimal.valueOf(80)) >= 0) healthStatus = "EXCELLENT";
        else if (overallHealthScore.compareTo(BigDecimal.valueOf(60)) >= 0) healthStatus = "GOOD";
        else if (overallHealthScore.compareTo(BigDecimal.valueOf(40)) >= 0) healthStatus = "FAIR";
        else if (overallHealthScore.compareTo(BigDecimal.valueOf(20)) >= 0) healthStatus = "POOR";
        else healthStatus = "CRITICAL";
        
        RepositoryHealthScoreDto.HealthBreakdownDto breakdown = new RepositoryHealthScoreDto.HealthBreakdownDto(
            BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.2)
        );
        
        return new RepositoryHealthScoreDto(
            activityScore, popularityScore, codeQualityScore, communityScore,
            overallHealthScore, healthStatus, breakdown
        );
    }
}

