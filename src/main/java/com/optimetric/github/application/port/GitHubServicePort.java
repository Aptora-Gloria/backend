package com.optimetric.github.application.port;

import com.optimetric.github.application.dto.GitHubOAuthResult;
import com.optimetric.github.application.dto.GitHubUserFromApi;

// GitHub OAuth ve kullanıcı bilgisi; token ve user döner.
public interface GitHubServicePort {
    GitHubOAuthResult getAccessTokenFromCode(String code);
    GitHubUserFromApi getUserInfo(String accessToken);
}
