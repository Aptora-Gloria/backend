package com.oobeya.github.application.port;

import com.oobeya.auth.api.dto.GitHubAccessTokenResponse;
import com.oobeya.auth.api.dto.GitHubUserDto;

public interface GitHubServicePort {
    GitHubAccessTokenResponse getAccessTokenFromCode(String code);
    GitHubUserDto getUserInfo(String accessToken);




}
