package com.optimetric.metrics.application.port;

import com.optimetric.metrics.application.dto.*;
import com.optimetric.shared.exception.UserNotFoundException;

public interface UserMetricsServicePort {

    UserMetricsInfo getUserMetrics(String login) throws UserNotFoundException;

    PrMergeRateInfo getPrMergeRate(String login) throws UserNotFoundException;

    PrSizeInfo getPrSize(String login) throws UserNotFoundException;

    ReviewStatsInfo getReviewStats(String login) throws UserNotFoundException;

    CommitTimeAnalysisInfo getCommitTimeAnalysis(String login) throws UserNotFoundException;

    LanguageDiversityInfo getLanguageDiversity(String login) throws UserNotFoundException;

    ActivityTrendInfo getActivityTrend(String login) throws UserNotFoundException;
}
