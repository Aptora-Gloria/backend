package com.oobeya.github.service;

import com.oobeya.github.dto.GitHubAccessTokenResponse;
import com.oobeya.github.dto.GitHubUserDto;

public interface GitHubServiceI {
    GitHubAccessTokenResponse getAccessTokenFromCode(String code);
    GitHubUserDto getUserInfo(String accessToken);




}
