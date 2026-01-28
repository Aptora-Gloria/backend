package com.optimetric.auth.application.port.out;

import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;

/**
 * Outbound port for GitHub OAuth operations.
 * This port abstracts GitHub API access from the application layer.
 */
public interface GitHubOAuthPort {

    /**
     * Exchanges authorization code for access token.
     * 
     * @param code Authorization code from GitHub OAuth callback
     * @return GitHub access token response
     * @throws com.optimetric.shared.exception.GitHubApiException if the exchange fails
     */
    GitHubAccessTokenResponse exchangeCodeForToken(String code);

    /**
     * Fetches user information from GitHub API.
     * 
     * @param accessToken GitHub access token
     * @return GitHub user information
     * @throws com.optimetric.shared.exception.GitHubApiException if the fetch fails
     */
    GitHubUserDto fetchUserInfo(String accessToken);
}
