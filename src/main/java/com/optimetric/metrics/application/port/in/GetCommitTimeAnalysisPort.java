package com.optimetric.metrics.application.port.in;

import com.optimetric.metrics.application.dto.CommitTimeAnalysisInfo;
import com.optimetric.shared.exception.UserNotFoundException;

public interface GetCommitTimeAnalysisPort {

    CommitTimeAnalysisInfo getCommitTimeAnalysis(String login) throws UserNotFoundException;
}
