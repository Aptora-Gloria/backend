package com.optimetric.github.infrastructure.http;

import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.application.client.PullRequestClientPort;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.util.GitHubUtil;
import com.optimetric.github.infrastructure.util.UrlBuilder;
import com.optimetric.shared.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PullRequestClientAdapter implements PullRequestClientPort {

    private final WebClient githubWebClient;

    @Override
    public List<PullRequestDto> getPullRequests(String accessToken, String owner, String repo, String state) {
        URI uri = UrlBuilder.fromTemplate(GitHubConstants.REPO_PULL_REQUESTS_ENDPOINT_TEMPLATE, owner, repo)
                .addQueryParam("state", state != null ? state : "all")
                .buildUri();

        return githubWebClient
                .get()
                .uri(uri)
                .headers(h -> {
                    h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken));
                    h.set("Accept", "application/vnd.github+json");
                    h.set("X-GitHub-Api-Version", "2022-11-28");
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_PULL_REQUEST_FETCH_FAILED + body)))
                .bodyToFlux(PullRequestDto.class)
                .collectList()
                .block();
    }
}


