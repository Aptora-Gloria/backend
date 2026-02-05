package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.PrMergeRateInfo;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetPrMergeRatePort {

    PrMergeRateInfo getPrMergeRate(String login) throws UserNotFoundException;
}
