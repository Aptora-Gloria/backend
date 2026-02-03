package com.optimetric.github.application.port.out;

import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;

/**
 * Outbound port for GitHub auth (access token, user info).
 */
public interface AuthClientPort {
    GitHubAccessTokenResponse getAccessToken(String code);
    GitHubUserDto getUserInfo(String accessToken);
}
