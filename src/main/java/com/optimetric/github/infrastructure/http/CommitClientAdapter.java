package com.optimetric.github.infrastructure.http;

import com.optimetric.github.api.response.CommitDto;
import com.optimetric.github.application.client.CommitClientPort;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.util.GitHubUtil;
import com.optimetric.shared.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommitClientAdapter implements CommitClientPort {

    private final WebClient githubWebClient;

    @Override
    public List<CommitDto> getCommits(String accessToken, String owner, String repo, String branch, String since) {
        String endpoint = String.format(GitHubConstants.REPO_COMMITS_ENDPOINT_TEMPLATE, owner, repo);
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(endpoint);
        
        if (branch != null && !branch.isBlank()) {
            uriBuilder.queryParam("sha", branch);
        }
        
        if (since != null && !since.isBlank()) {
            uriBuilder.queryParam("since", since);
        }
        
        return githubWebClient
                .get()
                .uri(uriBuilder.build().toUri())
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

