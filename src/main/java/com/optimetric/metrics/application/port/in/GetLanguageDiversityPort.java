package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.LanguageDiversityInfo;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetLanguageDiversityPort {

    LanguageDiversityInfo getLanguageDiversity(String login) throws UserNotFoundException;
}
