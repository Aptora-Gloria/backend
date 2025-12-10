package com.optimetric.github.application.port;

import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import java.util.List;

public interface PullRequestServicePort {

    /**
     * Fetches pull requests from GitHub API and saves them to database.
     * 
     * @param login User login
     * @param fullName Repository full name (e.g., "owner/repo")
     * @param state Pull request state (open, closed, all). If null, defaults to "all"
     * @return List of pull requests
     * @throws RepositoryNotFoundException if repository not found
     * @throws UserNotFoundException if user not found
     */
    List<PullRequestDto> initPullRequests(String login, String fullName, String state) 
            throws RepositoryNotFoundException, UserNotFoundException;

    /**
     * Gets pull requests from database for a repository.
     * 
     * @param login User login
     * @param fullName Repository full name (e.g., "owner/repo")
     * @param state Pull request state (open, closed, all). If null, returns all
     * @return List of pull requests
     * @throws RepositoryNotFoundException if repository not found
     * @throws UserNotFoundException if user not found
     */
    List<PullRequestDto> getPullRequestsByFullName(String login, String fullName, String state) 
            throws RepositoryNotFoundException, UserNotFoundException;
}


