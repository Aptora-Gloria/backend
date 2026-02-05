package com.optimetric.metrics.application.port.out;

import com.optimetric.metrics.domain.model.ActivityTrendInput;
import com.optimetric.metrics.domain.model.CommitMetricsInput;
import com.optimetric.metrics.domain.model.PrMetricsInput;
import com.optimetric.metrics.domain.model.ReviewMetricsInput;
import com.optimetric.metrics.domain.model.UserLanguageDiversityInput;
import com.optimetric.shared.exception.UserNotFoundException;

public interface UserMetricsDataPort {

    PrMetricsInput getPrMetricsInput(String login) throws UserNotFoundException;

    ReviewMetricsInput getReviewMetricsInput(String login) throws UserNotFoundException;

    CommitMetricsInput getCommitMetricsInput(String login) throws UserNotFoundException;

    ActivityTrendInput getActivityTrendInput(String login) throws UserNotFoundException;

    UserLanguageDiversityInput getLanguageDiversityInput(String login) throws UserNotFoundException;
}
