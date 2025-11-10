package com.oobeya.github.infrastructure.http;

import com.oobeya.auth.api.dto.GitHubAccessTokenResponse;
import com.oobeya.auth.api.dto.GitHubUserDto;
import com.oobeya.auth.api.dto.GitHubUserResponse;
import com.oobeya.github.api.response.BranchDto;
import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.response.RepoDto;
import com.oobeya.github.application.port.ApiClientPort;
import com.oobeya.github.infrastructure.core.config.GitHubOAuthProperties;
import com.oobeya.github.infrastructure.core.constant.GitHubConstants;
import com.oobeya.github.api.request.RepoInitrequest;
import com.oobeya.shared.exception.GitHubApiException;
import com.oobeya.github.infrastructure.util.GitHubUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ApiClientImpl implements ApiClientPort {

    private final WebClient githubWebClient;
    private final GitHubOAuthProperties properties;

    @Override
    public GitHubAccessTokenResponse getAccessToken(String code) {
        var tokenRequest = Map.of(
                "client_id", properties.getClientId(),
                "client_secret", properties.getClientSecret(),
                "code", code
        );

        return githubWebClient
                .post()
                .uri(GitHubConstants.TOKEN_ENDPOINT)
                .bodyValue(tokenRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_TOKEN_FETCH_FAILED + body)))
                .bodyToMono(GitHubAccessTokenResponse.class)
                .block();
    }

    @Override
    public GitHubUserDto getUserInfo(String accessToken) {
        // 1) GitHub API'den response al
        GitHubUserResponse response = githubWebClient
                .get()
                .uri(GitHubConstants.USER_ENDPOINT)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_USER_INFO_FAILED + body)))
                .bodyToMono(GitHubUserResponse.class)  // ← GitHubUserDto yerine GitHubUserResponse
                .block();

        // 2) Null kontrolü
        if (response == null) {
            throw new GitHubApiException("GitHub user info response is null");
        }

        // 3) GitHubUserResponse → GitHubUserDto'ya map et
        return new GitHubUserDto(
                null,                   // id (database id, henüz yok)
                response.id(),          // githubUserId ← GitHub'dan gelen id
                response.login(),
                null,                   // accessToken (AuthService'de set edilecek)
                response.name(),
                response.avatar_url(),
                response.htmlUrl(),
                response.type(),        // ownerType
                null                    // lastSyncedAt
        );
    }

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


    @Override
    public List<String> getRepoBranchNames(String accessToken, String ownerLogin, String repoName) {
        final int perPage = 100;
        int page = 1;
        List<String> result = new ArrayList<>();

        while (true) {
            String endpoint = String.format(
                    GitHubConstants.REPO_BRANCHES_ENDPOINT_TEMPLATE,
                    ownerLogin, repoName, perPage, page
            );

            List<BranchDto> pageData = githubWebClient
                    .get()
                    .uri(endpoint)
                    .headers(h -> {
                        h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken));
                        h.set("Accept", "application/vnd.github+json");
                        h.set("X-GitHub-Api-Version", "2022-11-28");
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res ->
                            res.bodyToMono(String.class).map(body ->
                                    new GitHubApiException(GitHubConstants.ERROR_REPO_FETCH_FAILED + body)))
                    .bodyToFlux(BranchDto.class)
                    .collectList()
                    .block();

            if (pageData == null || pageData.isEmpty()) break;

            for (BranchDto b : pageData) {
                if (b.name() != null && !b.name().isBlank()) result.add(b.name());
            }

            if (pageData.size() < perPage) break;
            page++;
        }

        return result;
    }



    @Override
    public List<CommitDto> getCommits(String accessToken, String owner, String repo) {//TODO
        String endpoint = String.format(GitHubConstants.REPO_COMMITS_ENDPOINT_TEMPLATE, owner, repo);
        return githubWebClient
                .get()
                .uri(endpoint)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_COMMIT_FETCH_FAILED + body)))
                .bodyToFlux(CommitDto.class)
                .collectList()
                .block();
    }

    @Override
    public LanguageDto getRepoLanguages(String accessToken, String owner, String repo) {
        String endpoint = String.format("https://api.github.com/repos/%s/%s/languages", owner, repo);

        @SuppressWarnings("unchecked")
        Map<String, Object> languages = githubWebClient
                .get()
                .uri(endpoint)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException("Failed to fetch languages: " + body)))
                .bodyToMono(Map.class)
                .block();

        if (languages == null || languages.isEmpty()) {
            return new LanguageDto(Map.of());
        }

        Map<String, Long> casted = languages.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> ((Number) e.getValue()).longValue()
                ));

        return new LanguageDto(casted);
    }














}
