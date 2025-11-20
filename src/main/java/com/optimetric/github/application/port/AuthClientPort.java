package com.optimetric.github.application.port;

import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;

public interface AuthClientPort {
    GitHubAccessTokenResponse getAccessToken(String code);
    GitHubUserDto getUserInfo(String accessToken);
}

