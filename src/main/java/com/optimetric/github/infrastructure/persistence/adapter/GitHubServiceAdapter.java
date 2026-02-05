package com.optimetric.github.infrastructure.persistence.adapter;

import org.springframework.stereotype.Service;

import com.optimetric.github.application.port.GitHubServicePort;
import com.optimetric.github.application.port.out.AuthClientPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GitHubServiceAdapter implements GitHubServicePort {

    private final AuthClientPort authClient;

    @Override
    public com.optimetric.github.application.dto.GitHubOAuthResult getAccessTokenFromCode(String code) {
        return authClient.getAccessToken(code);
    }

    @Override
    public com.optimetric.github.application.dto.GitHubUserFromApi getUserInfo(String accessToken) {
        return authClient.getUserInfo(accessToken);
    }
}
