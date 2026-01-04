package com.optimetric.metrics.application.port;

import com.optimetric.metrics.api.response.*;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

/**
 * Service port for repository metrics calculations.
 */
public interface RepositoryMetricsServicePort {
    
    /**
     * Gets comprehensive repository metrics including all metric types.
     * 
     * @param login User login (for authorization)
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return RepositoryMetricsDto containing all metrics
     * @throws RepositoryNotFoundException if repository not found
     * @throws UserNotFoundException if user not found
     */
    RepositoryMetricsDto getRepositoryMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets repository overview metrics.
     */
    RepositoryOverviewDto getOverview(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets Pull Request metrics.
     */
    RepositoryPrMetricsDto getPullRequestMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets commit metrics.
     */
    RepositoryCommitMetricsDto getCommitMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets review metrics.
     */
    RepositoryReviewMetricsDto getReviewMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets branch metrics.
     */
    RepositoryBranchMetricsDto getBranchMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets language metrics.
     */
    RepositoryLanguageMetricsDto getLanguageMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets activity trend metrics.
     */
    RepositoryActivityTrendDto getActivityTrend(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets code quality metrics.
     */
    RepositoryCodeQualityDto getCodeQuality(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets contributor metrics.
     */
    RepositoryContributorMetricsDto getContributorMetrics(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets repository health score.
     */
    RepositoryHealthScoreDto getHealthScore(String login, String repoFullName) 
            throws RepositoryNotFoundException, UserNotFoundException;
}

