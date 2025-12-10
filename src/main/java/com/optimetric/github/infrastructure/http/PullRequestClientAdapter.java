package com.optimetric.github.infrastructure.http;

import com.optimetric.github.api.response.CommitDto;
import com.optimetric.github.api.response.PullRequestCommentDto;
import com.optimetric.github.api.response.PullRequestDetailsDto;
import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.api.response.PullRequestReviewDto;
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

    @Override
    public PullRequestDetailsDto getPullRequestDetails(String accessToken, String owner, String repo, Integer pullNumber) {
        URI uri = UrlBuilder.fromTemplate(
                GitHubConstants.REPO_PULL_REQUEST_DETAILS_ENDPOINT_TEMPLATE, owner, repo, pullNumber
        ).buildUri();

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
                                new GitHubApiException(GitHubConstants.ERROR_PULL_REQUEST_DETAILS_FETCH_FAILED + body)))
                .bodyToMono(PullRequestDetailsDto.class)
                .block();
    }

    @Override
    public List<CommitDto> getPullRequestCommits(String accessToken, String owner, String repo, Integer pullNumber) {
        URI uri = UrlBuilder.fromTemplate(
                GitHubConstants.REPO_PULL_REQUEST_COMMITS_ENDPOINT_TEMPLATE, owner, repo, pullNumber
        ).buildUri();

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
                                new GitHubApiException(GitHubConstants.ERROR_PULL_REQUEST_COMMITS_FETCH_FAILED + body)))
                .bodyToFlux(CommitDto.class)
                .collectList()
                .block();
    }

    @Override
    public List<PullRequestReviewDto> getPullRequestReviews(String accessToken, String owner, String repo, Integer pullNumber) {
        URI uri = UrlBuilder.fromTemplate(
                GitHubConstants.REPO_PULL_REQUEST_REVIEWS_ENDPOINT_TEMPLATE, owner, repo, pullNumber
        ).buildUri();

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
                                new GitHubApiException(GitHubConstants.ERROR_PULL_REQUEST_REVIEWS_FETCH_FAILED + body)))
                .bodyToFlux(PullRequestReviewDto.class)
                .collectList()
                .block();
    }

    @Override
    public List<PullRequestCommentDto> getPullRequestComments(String accessToken, String owner, String repo, Integer pullNumber) {
        URI uri = UrlBuilder.fromTemplate(
                GitHubConstants.REPO_ISSUE_COMMENTS_ENDPOINT_TEMPLATE, owner, repo, pullNumber
        ).buildUri();

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
                                new GitHubApiException(GitHubConstants.ERROR_PULL_REQUEST_COMMENTS_FETCH_FAILED + body)))
                .bodyToFlux(PullRequestCommentDto.class)
                .collectList()
                .block();
    }
}


