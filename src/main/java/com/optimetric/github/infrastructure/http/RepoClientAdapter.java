package com.optimetric.github.infrastructure.http;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.optimetric.github.application.client.RepoClientPort;
import com.optimetric.github.application.dto.RepositoryInitData;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.core.mapper.GitHubRepositoryMapper;
import com.optimetric.github.infrastructure.http.dto.GitHubRepositoryResponse;
import com.optimetric.github.infrastructure.util.GitHubUtil;
import com.optimetric.shared.exception.GitHubApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepoClientAdapter implements RepoClientPort {

    private final WebClient githubWebClient;

    @Override
    public List<RepositoryInitData> getUserRepos(String accessToken) {
        List<GitHubRepositoryResponse> responses = githubWebClient
                .get()
                .uri(GitHubConstants.USER_REPOS_ENDPOINT)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_REPO_FETCH_FAILED + body)))
                .bodyToFlux(GitHubRepositoryResponse.class)
                .collectList()
                .block();
        
        return GitHubRepositoryMapper.toRepositoryInitDataList(responses);
    }
}

