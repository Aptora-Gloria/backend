package com.oobeya.github.service.impl;

import com.oobeya.github.dto.response.GitHubAccessTokenResponse;
import com.oobeya.github.dto.response.GitHubUserDto;
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
