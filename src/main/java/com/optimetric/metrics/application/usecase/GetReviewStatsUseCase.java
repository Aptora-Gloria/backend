package com.optimetric.metrics.application.usecase;

import com.optimetric.metrics.application.dto.ReviewStatsInfo;
import com.optimetric.metrics.application.port.in.GetReviewStatsPort;
import com.optimetric.metrics.application.port.out.UserMetricsDataPort;
import com.optimetric.metrics.domain.model.ReviewMetricsResult;
import com.optimetric.metrics.domain.service.ReviewMetricsCalculator;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetReviewStatsUseCase implements GetReviewStatsPort {

    private final UserMetricsDataPort userMetricsDataPort;

    @Override
    public ReviewStatsInfo getReviewStats(String login) throws UserNotFoundException {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login cannot be null or blank");
        }
        var input = userMetricsDataPort.getReviewMetricsInput(login.trim());
        ReviewMetricsResult result = ReviewMetricsCalculator.calculate(input);
        return toInfo(result);
    }

    private static ReviewStatsInfo toInfo(ReviewMetricsResult r) {
        return new ReviewStatsInfo(
                r.totalReviews(), r.approvedReviews(), r.changesRequestedReviews(), r.commentedReviews(),
                r.averageReviewsPerPr(), r.approvalRate(), r.reviewsByState(),
                r.prsWithoutReviews(), r.prsWithReviewsPercentage()
        );
    }
}
