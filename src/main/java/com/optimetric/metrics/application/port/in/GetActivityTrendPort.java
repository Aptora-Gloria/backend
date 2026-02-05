package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.ActivityTrendInfo;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetActivityTrendPort {

    ActivityTrendInfo getActivityTrend(String login) throws UserNotFoundException;
}
