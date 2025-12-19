package com.optimetric.github.infrastructure.persistence.adapter;

import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;
import com.optimetric.github.application.client.AuthClientPort;
import com.optimetric.github.application.port.GitHubServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubServiceAdapter implements GitHubServicePort {

    private final AuthClientPort authClient;

    @Override
    public GitHubAccessTokenResponse getAccessTokenFromCode(String code) {
        return authClient.getAccessToken(code);
    }

    @Override
    public GitHubUserDto getUserInfo(String accessToken) {
        return authClient.getUserInfo(accessToken);
    }
}
