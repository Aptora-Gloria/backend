package com.optimetric.github.application.port.out;

import com.optimetric.github.application.dto.GitHubOAuthResult;
import com.optimetric.github.application.dto.GitHubUserFromApi;

public interface AuthClientPort {
    GitHubOAuthResult getAccessToken(String code);
    GitHubUserFromApi getUserInfo(String accessToken);
}
