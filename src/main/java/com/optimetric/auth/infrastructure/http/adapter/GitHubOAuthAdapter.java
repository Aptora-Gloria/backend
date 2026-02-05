package com.optimetric.auth.infrastructure.http.adapter;

import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;
import com.optimetric.auth.application.port.out.GitHubOAuthPort;
import com.optimetric.github.application.dto.GitHubOAuthResult;
import com.optimetric.github.application.dto.GitHubUserFromApi;
import com.optimetric.github.application.port.GitHubServicePort;
import com.optimetric.shared.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// GitHub OAuth işlemleri için altyapı adapter'ı; GitHubOAuthPort uygular.
@Component
@RequiredArgsConstructor
public class GitHubOAuthAdapter implements GitHubOAuthPort {

    private final GitHubServicePort githubServicePort;

    @Override
    public GitHubAccessTokenResponse exchangeCodeForToken(String code) {
        try {
            GitHubOAuthResult result = githubServicePort.getAccessTokenFromCode(code);
            if (result == null || result.accessToken() == null) {
                throw new GitHubApiException("GitHub access token result is null");
            }
            return new GitHubAccessTokenResponse(result.accessToken(), "Bearer", null);
        } catch (GitHubApiException e) {
            throw new GitHubApiException("Failed to exchange authorization code for access token: " + e.getMessage(), e);
        }
    }

    @Override
    public GitHubUserDto fetchUserInfo(String accessToken) {
        try {
            GitHubUserFromApi user = githubServicePort.getUserInfo(accessToken);
            return new GitHubUserDto(
                    null,
                    user.githubUserId(),
                    user.login(),
                    accessToken,
                    user.name(),
                    user.avatarUrl(),
                    user.htmlUrl(),
                    user.type(),
                    null
            );
        } catch (GitHubApiException e) {
            throw new GitHubApiException("Failed to fetch user info from GitHub: " + e.getMessage(), e);
        }
    }
}
