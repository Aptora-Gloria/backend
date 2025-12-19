package com.optimetric.github.application.port;

import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;

public interface GitHubServicePort {
    GitHubAccessTokenResponse getAccessTokenFromCode(String code);
    GitHubUserDto getUserInfo(String accessToken);




}
