package com.optimetric.github.application.client;

import com.optimetric.github.api.response.CommitDto;
import com.optimetric.github.api.response.PullRequestCommentDto;
import com.optimetric.github.api.response.PullRequestDetailsDto;
import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.api.response.PullRequestReviewDto;

import java.util.List;

public interface PullRequestClientPort {
    
    /**
     * Gets pull requests for a repository from GitHub API.
     * 
     * @param accessToken GitHub access token
     * @param owner Repository owner login
     * @param repo Repository name
     * @param state Pull request state (open, closed, all). If null, defaults to "all"
     * @return List of pull requests
     */
    List<PullRequestDto> getPullRequests(String accessToken, String owner, String repo, String state);
    
    /**
     * Gets detailed information for a specific pull request.
     * 
     * @param accessToken GitHub access token
     * @param owner Repository owner login
     * @param repo Repository name
     * @param pullNumber Pull request number
     * @return Pull request details with metrics
     */
    PullRequestDetailsDto getPullRequestDetails(String accessToken, String owner, String repo, Integer pullNumber);
    
    /**
     * Gets commits for a specific pull request.
     * 
     * @param accessToken GitHub access token
     * @param owner Repository owner login
     * @param repo Repository name
     * @param pullNumber Pull request number
     * @return List of commits in the pull request
     */
    List<CommitDto> getPullRequestCommits(String accessToken, String owner, String repo, Integer pullNumber);
    
    /**
     * Gets reviews for a specific pull request.
     * 
     * @param accessToken GitHub access token
     * @param owner Repository owner login
     * @param repo Repository name
     * @param pullNumber Pull request number
     * @return List of reviews
     */
    List<PullRequestReviewDto> getPullRequestReviews(String accessToken, String owner, String repo, Integer pullNumber);
    
    /**
     * Gets comments for a specific pull request (via issues endpoint).
     * 
     * @param accessToken GitHub access token
     * @param owner Repository owner login
     * @param repo Repository name
     * @param pullNumber Pull request number (same as issue number)
     * @return List of comments
     */
    List<PullRequestCommentDto> getPullRequestComments(String accessToken, String owner, String repo, Integer pullNumber);
}


