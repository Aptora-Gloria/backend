package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.ReviewStatsInfo;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetReviewStatsPort {

    ReviewStatsInfo getReviewStats(String login) throws UserNotFoundException;
}
