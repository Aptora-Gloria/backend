package com.optimetric.github.infrastructure.http;

import com.optimetric.auth.api.dto.GitHubAccessTokenResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;
import com.optimetric.auth.api.dto.GitHubUserResponse;
import com.optimetric.github.application.client.AuthClientPort;
import com.optimetric.github.infrastructure.core.config.GitHubOAuthProperties;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.util.GitHubUtil;
import com.optimetric.shared.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AuthClientAdapter implements AuthClientPort {

    private final WebClient githubWebClient;
    private final GitHubOAuthProperties properties;

    @Override
    public GitHubAccessTokenResponse getAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", properties.getClientId());
        formData.add("client_secret", properties.getClientSecret());
        formData.add("code", code);

        return githubWebClient
                .post()
                .uri(GitHubConstants.TOKEN_ENDPOINT)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_TOKEN_FETCH_FAILED + body)))
                .bodyToMono(GitHubAccessTokenResponse.class)
                .block();
    }

    @Override
    public GitHubUserDto getUserInfo(String accessToken) {
        GitHubUserResponse response = githubWebClient
                .get()
                .uri(GitHubConstants.USER_ENDPOINT)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_USER_INFO_FAILED + body)))
                .bodyToMono(GitHubUserResponse.class)
                .block();

        if (response == null) {
            throw new GitHubApiException("GitHub user info response is null");
        }

        return new GitHubUserDto(
                null,
                response.id(),
                response.login(),
                null,
                response.name(),
                response.avatar_url(),
                response.htmlUrl(),
                response.type(),
                null
        );
    }
}

