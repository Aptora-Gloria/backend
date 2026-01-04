package com.optimetric.metrics.application.port;

import com.optimetric.metrics.api.response.*;
import com.optimetric.shared.exception.UserNotFoundException;

/**
 * Service port for user metrics calculations.
 */
public interface UserMetricsServicePort {
    
    /**
     * Gets comprehensive user metrics including all metric types.
     * 
     * @param login User login
     * @return UserMetricsDto containing all metrics
     * @throws UserNotFoundException if user not found
     */
    UserMetricsDto getUserMetrics(String login) throws UserNotFoundException;
    
    /**
     * Gets Pull Request merge rate metrics.
     * 
     * @param login User login
     * @return PrMergeRateDto
     * @throws UserNotFoundException if user not found
     */
    PrMergeRateDto getPrMergeRate(String login) throws UserNotFoundException;
    
    /**
     * Gets Pull Request size metrics.
     * 
     * @param login User login
     * @return PrSizeDto
     * @throws UserNotFoundException if user not found
     */
    PrSizeDto getPrSize(String login) throws UserNotFoundException;
    
    /**
     * Gets review statistics metrics.
     * 
     * @param login User login
     * @return ReviewStatsDto
     * @throws UserNotFoundException if user not found
     */
    ReviewStatsDto getReviewStats(String login) throws UserNotFoundException;
    
    /**
     * Gets commit time analysis metrics.
     * 
     * @param login User login
     * @return CommitTimeAnalysisDto
     * @throws UserNotFoundException if user not found
     */
    CommitTimeAnalysisDto getCommitTimeAnalysis(String login) throws UserNotFoundException;
    
    /**
     * Gets language diversity metrics.
     * 
     * @param login User login
     * @return LanguageDiversityDto
     * @throws UserNotFoundException if user not found
     */
    LanguageDiversityDto getLanguageDiversity(String login) throws UserNotFoundException;
    
    /**
     * Gets activity trend metrics.
     * 
     * @param login User login
     * @return ActivityTrendDto
     * @throws UserNotFoundException if user not found
     */
    ActivityTrendDto getActivityTrend(String login) throws UserNotFoundException;
}

