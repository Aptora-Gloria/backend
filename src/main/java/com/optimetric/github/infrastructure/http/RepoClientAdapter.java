package com.optimetric.github.infrastructure.http;

import com.optimetric.github.api.request.RepoInitrequest;
import com.optimetric.github.application.client.RepoClientPort;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.util.GitHubUtil;
import com.optimetric.shared.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepoClientAdapter implements RepoClientPort {

    private final WebClient githubWebClient;

    @Override
    public List<RepoInitrequest> getUserRepos(String accessToken) {
        return githubWebClient
                .get()
                .uri(GitHubConstants.USER_REPOS_ENDPOINT)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_REPO_FETCH_FAILED + body)))
                .bodyToFlux(RepoInitrequest.class)
                .collectList()
                .block();
    }
}

