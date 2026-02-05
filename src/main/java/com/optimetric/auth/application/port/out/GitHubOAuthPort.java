package com.optimetric.auth.application.port.out;

import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;

public interface GitHubOAuthPort {

    GitHubAccessTokenResponse exchangeCodeForToken(String code);

    GitHubUserDto fetchUserInfo(String accessToken);
}
