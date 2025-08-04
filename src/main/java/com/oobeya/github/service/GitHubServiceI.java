package com.oobeya.github.service;

import com.oobeya.github.dto.response.GitHubAccessTokenResponse;
import com.oobeya.github.dto.response.GitHubUserDto;

public interface GitHubServiceI {
    GitHubAccessTokenResponse getAccessTokenFromCode(String code);
    GitHubUserDto getUserInfo(String accessToken);




}
