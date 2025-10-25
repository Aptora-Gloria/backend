package com.oobeya.github.service.impl;

import com.oobeya.auth.dto.GitHubAccessTokenResponse;
import com.oobeya.auth.dto.GitHubUserDto;
import com.oobeya.github.service.ApiClientI;
import com.oobeya.github.service.GitHubServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class GitHubServiceImpl implements GitHubServiceI {



    private final ApiClientI apiClientI;

    @Override
    public GitHubAccessTokenResponse getAccessTokenFromCode(String code) {
        return apiClientI.getAccessToken(code);
    }
    @Override
    public GitHubUserDto getUserInfo(String accessToken) {
        return apiClientI.getUserInfo(accessToken);
    }



}
