package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.PrSizeInfo;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetPrSizePort {

    PrSizeInfo getPrSize(String login) throws UserNotFoundException;
}
