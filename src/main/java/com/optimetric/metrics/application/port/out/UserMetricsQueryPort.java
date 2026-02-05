package com.optimetric.metrics.application.port.out;

import com.optimetric.metrics.application.dto.ActivityTrendInfo;
import com.optimetric.metrics.application.dto.CommitTimeAnalysisInfo;
import com.optimetric.metrics.application.dto.LanguageDiversityInfo;
import com.optimetric.metrics.application.dto.PrMergeRateInfo;
import com.optimetric.metrics.application.dto.PrSizeInfo;
import com.optimetric.metrics.application.dto.ReviewStatsInfo;
import com.optimetric.shared.exception.UserNotFoundException;

public interface UserMetricsQueryPort {

    PrMergeRateInfo getPrMergeRateInfo(String login) throws UserNotFoundException;

    PrSizeInfo getPrSizeInfo(String login) throws UserNotFoundException;

    ReviewStatsInfo getReviewStatsInfo(String login) throws UserNotFoundException;

    CommitTimeAnalysisInfo getCommitTimeAnalysisInfo(String login) throws UserNotFoundException;

    LanguageDiversityInfo getLanguageDiversityInfo(String login) throws UserNotFoundException;

    ActivityTrendInfo getActivityTrendInfo(String login) throws UserNotFoundException;
}
