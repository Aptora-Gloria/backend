package com.optimetric.metrics.infrastructure.persistence.adapter;

import com.optimetric.metrics.application.dto.ActivityTrendInfo;
import com.optimetric.metrics.application.dto.CommitTimeAnalysisInfo;
import com.optimetric.metrics.application.dto.LanguageDiversityInfo;
import com.optimetric.metrics.application.dto.PrMergeRateInfo;
import com.optimetric.metrics.application.dto.PrSizeInfo;
import com.optimetric.metrics.application.dto.ReviewStatsInfo;
import com.optimetric.metrics.application.dto.UserMetricsInfo;
import com.optimetric.metrics.application.port.UserMetricsServicePort;
import com.optimetric.metrics.application.port.out.UserMetricsDataPort;
import com.optimetric.metrics.application.port.out.UserMetricsQueryPort;
import com.optimetric.metrics.domain.model.*;
import com.optimetric.metrics.infrastructure.persistence.mapper.RepositoryMetricsPersistenceMapper;
import com.optimetric.metrics.infrastructure.persistence.mapper.UserMetricsPersistenceMapper;
import com.optimetric.github.application.dto.CommitInfo;
import com.optimetric.github.application.dto.PullRequestInfo;
import com.optimetric.github.application.dto.RepositoryInfo;
import com.optimetric.github.application.dto.ReviewInfo;
import com.optimetric.github.application.port.in.GetCommitsPort;
import com.optimetric.github.application.port.in.GetLanguagesPort;
import com.optimetric.github.application.port.in.GetPullRequestReviewsPort;
import com.optimetric.github.application.port.in.GetPullRequestsPort;
import com.optimetric.github.application.port.in.QueryRepositoriesPort;
import com.optimetric.metrics.domain.service.ActivityTrendCalculator;
import com.optimetric.metrics.domain.service.CommitMetricsCalculator;
import com.optimetric.metrics.domain.service.PrMetricsCalculator;
import com.optimetric.metrics.domain.service.ReviewMetricsCalculator;
import com.optimetric.metrics.domain.service.UserLanguageDiversityCalculator;
import com.optimetric.shared.exception.BranchNotFoundException;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMetricsServiceAdapter implements UserMetricsServicePort, UserMetricsQueryPort, UserMetricsDataPort {

    private final QueryRepositoriesPort queryRepositoriesPort;
    private final GetPullRequestsPort getPullRequestsPort;
    private final GetPullRequestReviewsPort getPullRequestReviewsPort;
    private final GetCommitsPort getCommitsPort;
    private final GetLanguagesPort getLanguagesPort;

    private List<RepositoryInfo> getRepositoriesForUser(String login) throws UserNotFoundException {
        return queryRepositoriesPort.execute(login, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PrMetricsInput getPrMetricsInput(String login) throws UserNotFoundException {
        List<RepositoryInfo> repositories = getRepositoriesForUser(login);
        List<PrRaw> rawList = repositories.stream()
                .flatMap(repo -> getPullRequestsForUserRepo(login, repo.fullName()).stream())
                .map(RepositoryMetricsPersistenceMapper::toPrRaw)
                .toList();
        return new PrMetricsInput(rawList);
    }

    private List<PullRequestInfo> getPullRequestsForUserRepo(String login, String repoFullName) {
        try {
            return getPullRequestsPort.getPullRequestsForRepository(login, repoFullName);
        } catch (RepositoryNotFoundException e) {
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewMetricsInput getReviewMetricsInput(String login) throws UserNotFoundException {
        List<RepositoryInfo> repositories = getRepositoriesForUser(login);
        List<PullRequestInfo> allPrs = repositories.stream()
                .flatMap(repo -> getPullRequestsForUserRepo(login, repo.fullName()).stream())
                .toList();
        List<ReviewRaw> reviews = allPrs.stream()
                .flatMap(pr -> getReviewsForUserPr(login, pr.repoFullName(), pr.prNumber()).stream())
                .map(RepositoryMetricsPersistenceMapper::toReviewRaw)
                .toList();
        long totalPrCount = allPrs.size();
        long prsWithoutReviews = allPrs.stream()
                .filter(pr -> getReviewsForUserPr(login, pr.repoFullName(), pr.prNumber()).isEmpty())
                .count();
        return new ReviewMetricsInput(reviews, totalPrCount, prsWithoutReviews);
    }

    private List<ReviewInfo> getReviewsForUserPr(String login, String repoFullName, Integer prNumber) {
        try {
            return getPullRequestReviewsPort.getPullRequestReviews(login, repoFullName, prNumber);
        } catch (RepositoryNotFoundException e) {
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CommitMetricsInput getCommitMetricsInput(String login) throws UserNotFoundException {
        List<RepositoryInfo> repositories = getRepositoriesForUser(login);
        List<CommitRaw> rawList = repositories.stream()
                .flatMap(repo -> getCommitsForUserRepo(login, repo.fullName()).stream())
                .map(RepositoryMetricsPersistenceMapper::toCommitRaw)
                .toList();
        return new CommitMetricsInput(rawList);
    }

    private List<CommitInfo> getCommitsForUserRepo(String login, String repoFullName) {
        try {
            return getCommitsPort.getCommits(login, repoFullName, null);
        } catch (RepositoryNotFoundException | BranchNotFoundException e) {
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityTrendInput getActivityTrendInput(String login) throws UserNotFoundException {
        List<RepositoryInfo> repositories = getRepositoriesForUser(login);
        OffsetDateTime twelveWeeksAgo = OffsetDateTime.now().minusWeeks(12);
        List<CommitInfo> commits = repositories.stream()
                .flatMap(repo -> getCommitsForUserRepo(login, repo.fullName()).stream())
                .filter(c -> c.committedAt() != null && c.committedAt().isAfter(twelveWeeksAgo))
                .toList();
        List<PullRequestInfo> prs = repositories.stream()
                .flatMap(repo -> getPullRequestsForUserRepo(login, repo.fullName()).stream())
                .filter(pr -> pr.createdAt() != null && pr.createdAt().isAfter(twelveWeeksAgo))
                .toList();
        List<ReviewInfo> reviews = prs.stream()
                .flatMap(pr -> getReviewsForUserPr(login, pr.repoFullName(), pr.prNumber()).stream())
                .filter(r -> r.submittedAt() != null && r.submittedAt().isAfter(twelveWeeksAgo))
                .toList();
        List<OffsetDateTime> commitDates = commits.stream().map(CommitInfo::committedAt).filter(Objects::nonNull).toList();
        List<OffsetDateTime> prDates = prs.stream().map(PullRequestInfo::createdAt).filter(Objects::nonNull).toList();
        List<OffsetDateTime> reviewDates = reviews.stream().map(ReviewInfo::submittedAt).filter(Objects::nonNull).toList();
        return new ActivityTrendInput(commitDates, prDates, reviewDates);
    }

    @Override
    @Transactional(readOnly = true)
    public UserLanguageDiversityInput getLanguageDiversityInput(String login) throws UserNotFoundException {
        List<RepositoryInfo> repositories = getRepositoriesForUser(login);
        List<UserLanguageDiversityInput.RepoLanguagesRaw> repos = repositories.stream()
                .map(repo -> {
                    var languageInfo = getLanguagesPort.getLanguages(repo.fullName());
                    List<LanguageRaw> langs = RepositoryMetricsPersistenceMapper.toLanguageRawList(languageInfo);
                    return new UserLanguageDiversityInput.RepoLanguagesRaw(langs);
                })
                .toList();
        return new UserLanguageDiversityInput(repos);
    }

    @Override
    @Transactional(readOnly = true)
    public UserMetricsInfo getUserMetrics(String login) throws UserNotFoundException {
        return new UserMetricsInfo(
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
    public PrMergeRateInfo getPrMergeRateInfo(String login) throws UserNotFoundException {
        PrMetricsInput input = getPrMetricsInput(login);
        PrMetricsResult result = PrMetricsCalculator.calculate(input);
        return UserMetricsPersistenceMapper.toPrMergeRateInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PrMergeRateInfo getPrMergeRate(String login) throws UserNotFoundException {
        return getPrMergeRateInfo(login);
    }

    @Override
    @Transactional(readOnly = true)
    public PrSizeInfo getPrSizeInfo(String login) throws UserNotFoundException {
        PrMetricsInput input = getPrMetricsInput(login);
        PrMetricsResult result = PrMetricsCalculator.calculate(input);
        return UserMetricsPersistenceMapper.toPrSizeInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PrSizeInfo getPrSize(String login) throws UserNotFoundException {
        return getPrSizeInfo(login);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewStatsInfo getReviewStatsInfo(String login) throws UserNotFoundException {
        ReviewMetricsInput input = getReviewMetricsInput(login);
        ReviewMetricsResult result = ReviewMetricsCalculator.calculate(input);
        return UserMetricsPersistenceMapper.toReviewStatsInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewStatsInfo getReviewStats(String login) throws UserNotFoundException {
        return getReviewStatsInfo(login);
    }

    @Override
    @Transactional(readOnly = true)
    public CommitTimeAnalysisInfo getCommitTimeAnalysisInfo(String login) throws UserNotFoundException {
        CommitMetricsInput input = getCommitMetricsInput(login);
        CommitMetricsResult result = CommitMetricsCalculator.calculate(input);
        return UserMetricsPersistenceMapper.toCommitTimeAnalysisInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public CommitTimeAnalysisInfo getCommitTimeAnalysis(String login) throws UserNotFoundException {
        return getCommitTimeAnalysisInfo(login);
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageDiversityInfo getLanguageDiversityInfo(String login) throws UserNotFoundException {
        UserLanguageDiversityInput input = getLanguageDiversityInput(login);
        UserLanguageDiversityResult result = UserLanguageDiversityCalculator.calculate(input);
        return UserMetricsPersistenceMapper.toLanguageDiversityInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageDiversityInfo getLanguageDiversity(String login) throws UserNotFoundException {
        return getLanguageDiversityInfo(login);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityTrendInfo getActivityTrendInfo(String login) throws UserNotFoundException {
        ActivityTrendInput input = getActivityTrendInput(login);
        ActivityTrendResult result = ActivityTrendCalculator.calculate(input);
        return UserMetricsPersistenceMapper.toActivityTrendInfo(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityTrendInfo getActivityTrend(String login) throws UserNotFoundException {
        return getActivityTrendInfo(login);
    }
}

