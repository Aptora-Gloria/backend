package com.optimetric.github.infrastructure.http;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.optimetric.github.api.response.CommitDto;
import com.optimetric.github.application.client.CommitClientPort;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.util.GitHubUtil;
import com.optimetric.github.infrastructure.util.UrlBuilder;
import com.optimetric.shared.exception.GitHubApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommitClientAdapter implements CommitClientPort {

    private final WebClient githubWebClient;

    @Override
    public List<CommitDto> getCommits(String accessToken, String owner, String repo, String branch, String since) {
        URI uri = UrlBuilder.fromTemplate(GitHubConstants.REPO_COMMITS_ENDPOINT_TEMPLATE, owner, repo)
                .addQueryParam("sha", branch)
                .addQueryParam("since", since)
                .buildUri();
        
        return githubWebClient
                .get()
                .uri(uri)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_COMMIT_FETCH_FAILED + body)))
                .bodyToFlux(CommitDto.class)
                .collectList()
                .block();
    }
}

