package com.oobeya.github.service.impl;

import com.oobeya.github.config.GitHubOAuthProperties;
import com.oobeya.github.constant.GitHubConstants;
import com.oobeya.github.dto.response.*;
import com.oobeya.github.exception.GitHubApiException;
import com.oobeya.github.service.ApiClientI;
import com.oobeya.github.util.GitHubUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ApiClientImpl implements ApiClientI {

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
        return githubWebClient
                .get()
                .uri(GitHubConstants.USER_ENDPOINT)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_USER_INFO_FAILED + body)))
                .bodyToMono(GitHubUserDto.class)
                .block();
    }

    @Override
    public List<RepoDto> getUserRepos(String accessToken) {
        return githubWebClient
                .get()
                .uri(GitHubConstants.USER_REPOS_ENDPOINT)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_REPO_FETCH_FAILED + body)))
                .bodyToFlux(RepoDto.class)
                .collectList()
                .block();
    }

    @Override
    public List<ContentDto> getRepoContents(String accessToken, String owner, String repo, String path) {
        String endpoint = String.format(GitHubConstants.REPO_CONTENTS_ENDPOINT_TEMPLATE, owner, repo, path != null ? path : "");
        return githubWebClient
                .get()
                .uri(endpoint)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_REPO_CONTENTS_FAILED + body)))
                .bodyToFlux(ContentDto.class)
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

            List<com.oobeya.github.dto.response.BranchDto> pageData = githubWebClient
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
                    .bodyToFlux(com.oobeya.github.dto.response.BranchDto.class)
                    .collectList()
                    .block();

            if (pageData == null || pageData.isEmpty()) break;

            for (com.oobeya.github.dto.response.BranchDto b : pageData) {
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

    @Override
    public List<OrganizationDto> getUserOrganizations(String accessToken) {
        String url = "https://api.github.com/user/orgs";

        return githubWebClient
                .get()
                .uri(url)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException("Failed to fetch organizations: " + body)))
                .bodyToFlux(OrganizationDto.class)
                .collectList()
                .block();
    }

    @Override
    public List<RepoDto> getOrgRepos(String accessToken, String orgLogin) {
        String endpoint = String.format("https://api.github.com/orgs/%s/repos", orgLogin);

        return githubWebClient
                .get()
                .uri(endpoint)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException(GitHubConstants.ERROR_REPO_FETCH_FAILED + body)))
                .bodyToFlux(RepoDto.class)
                .collectList()
                .block();
    }




    @Override
    public List<ContributorStatsDto> fetchContributorStats(String accessToken, String owner, String repo) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/stats/contributors";

        // 1. Ham JSON metni al
        String rawJson = githubWebClient.get()
                .uri(url)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException("Commit activity fetch failed: " + body)))
                .bodyToMono(String.class)
                .block();

        // 2. null ya da boş kontrolü
        if (rawJson == null || rawJson.isBlank()) {
                throw new GitHubApiException("Empty response from GitHub contributor stats");
        }

        // 3. Eğer { ile başlıyorsa, muhtemelen mesaj içeriyordur (202 gibi)
        if (rawJson.trim().startsWith("{")) {
                throw new GitHubApiException("GitHub contributor stats not ready: " + rawJson);
        }

        try {
                // 4. JSON string'i ContributorStatsDto dizisine parse et
                ContributorStatsDto[] stats = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(rawJson, ContributorStatsDto[].class);
                return Arrays.asList(stats);
        } catch (Exception e) {
                throw new GitHubApiException("JSON parse error for contributor stats: " + e.getMessage());
        }
        }





}
