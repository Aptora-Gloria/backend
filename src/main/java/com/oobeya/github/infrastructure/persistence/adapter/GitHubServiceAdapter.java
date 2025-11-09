package com.oobeya.github.infrastructure.persistence.adapter;

import com.oobeya.auth.api.dto.GitHubAccessTokenResponse;
import com.oobeya.auth.api.dto.GitHubUserDto;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.application.port.GitHubServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class GitHubServiceAdapter implements GitHubServicePort {



    private final ApiClientPort apiClientI;

    @Override
    public GitHubAccessTokenResponse getAccessTokenFromCode(String code) {
        return apiClientI.getAccessToken(code);
    }
    @Override
    public GitHubUserDto getUserInfo(String accessToken) {
        return apiClientI.getUserInfo(accessToken);
    }



}
