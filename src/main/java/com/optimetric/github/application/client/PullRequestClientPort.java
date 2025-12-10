package com.optimetric.github.application.client;

import com.optimetric.github.api.response.PullRequestDto;

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
}


