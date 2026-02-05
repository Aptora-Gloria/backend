package com.optimetric.github.infrastructure.http.adapter;

import com.optimetric.github.application.dto.GitHubOAuthResult;
import com.optimetric.github.application.dto.GitHubUserFromApi;
import com.optimetric.github.application.port.out.AuthClientPort;
import com.optimetric.github.infrastructure.core.config.GitHubOAuthProperties;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.util.GitHubUtil;
import com.optimetric.github.infrastructure.http.dto.GitHubTokenApiResponse;
import com.optimetric.github.infrastructure.http.dto.GitHubUserApiResponse;
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
    public GitHubOAuthResult getAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", properties.getClientId());
        formData.add("client_secret", properties.getClientSecret());
        formData.add("code", code);

        GitHubTokenApiResponse response = githubWebClient
                .post()
                .uri(GitHubConstants.TOKEN_ENDPOINT)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_TOKEN_FETCH_FAILED + body)))
                .bodyToMono(GitHubTokenApiResponse.class)
                .block();
        return response != null ? new GitHubOAuthResult(response.accessToken()) : null;
    }

    @Override
    public GitHubUserFromApi getUserInfo(String accessToken) {
        GitHubUserApiResponse response = githubWebClient
                .get()
                .uri(GitHubConstants.USER_ENDPOINT)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_USER_INFO_FAILED + body)))
                .bodyToMono(GitHubUserApiResponse.class)
                .block();

        if (response == null) {
            throw new GitHubApiException("GitHub user info response is null");
        }

        return new GitHubUserFromApi(
                response.id(),
                response.login(),
                response.name(),
                response.avatarUrl(),
                response.htmlUrl(),
                response.type()
        );
    }
}

