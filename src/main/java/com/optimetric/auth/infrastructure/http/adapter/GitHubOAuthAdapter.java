package com.optimetric.auth.infrastructure.http.adapter;

import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;
import com.optimetric.auth.application.port.out.GitHubOAuthPort;
import com.optimetric.github.application.port.GitHubServicePort;
import com.optimetric.shared.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Infrastructure adapter for GitHub OAuth operations.
 * Implements GitHubOAuthPort using GitHubServicePort.
 */
@Component
@RequiredArgsConstructor
public class GitHubOAuthAdapter implements GitHubOAuthPort {

    private final GitHubServicePort githubServicePort;

    @Override
    public GitHubAccessTokenResponse exchangeCodeForToken(String code) {
        try {
            return githubServicePort.getAccessTokenFromCode(code);
        } catch (GitHubApiException e) {
            throw new GitHubApiException("Failed to exchange authorization code for access token: " + e.getMessage(), e);
        }
    }

    @Override
    public GitHubUserDto fetchUserInfo(String accessToken) {
        try {
            GitHubUserDto userDto = githubServicePort.getUserInfo(accessToken);
            
            // Add accessToken to DTO (record is immutable, so create new instance)
            return new GitHubUserDto(
                    userDto.id(),
                    userDto.githubUserId(),
                    userDto.login(),
                    accessToken,
                    userDto.name(),
                    userDto.avatar_url(),
                    userDto.htmlUrl(),
                    userDto.ownerType(),
                    userDto.lastSyncedAt()
            );
        } catch (GitHubApiException e) {
            throw new GitHubApiException("Failed to fetch user info from GitHub: " + e.getMessage(), e);
        }
    }
}
