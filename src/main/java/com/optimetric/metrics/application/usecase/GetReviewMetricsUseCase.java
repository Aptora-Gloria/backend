package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.RepositoryReviewMetricsInfo;
import com.optimetric.metrics.application.port.in.GetReviewMetricsPort;
import com.optimetric.metrics.application.port.out.RepositoryMetricsDataPort;
import com.optimetric.metrics.domain.model.ReviewMetricsResult;
import com.optimetric.metrics.domain.service.ReviewMetricsCalculator;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetReviewMetricsUseCase implements GetReviewMetricsPort {

    private final RepositoryMetricsDataPort repositoryMetricsDataPort;

    @Override
    public RepositoryReviewMetricsInfo getReviewMetrics(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        if (repoFullName == null || repoFullName.isBlank()) {
            throw new IllegalArgumentException("repoFullName cannot be null or blank");
        }
        var input = repositoryMetricsDataPort.getReviewMetricsInput(login, repoFullName.trim());
        ReviewMetricsResult result = ReviewMetricsCalculator.calculate(input);
        return toInfo(result);
    }

    private static RepositoryReviewMetricsInfo toInfo(ReviewMetricsResult r) {
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
}
