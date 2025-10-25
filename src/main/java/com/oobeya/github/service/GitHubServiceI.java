package com.oobeya.github.service;

import com.oobeya.auth.dto.GitHubAccessTokenResponse;
import com.oobeya.auth.dto.GitHubUserDto;

public interface GitHubServiceI {
    GitHubAccessTokenResponse getAccessTokenFromCode(String code);
    GitHubUserDto getUserInfo(String accessToken);




}
