package com.optimetric.github.infrastructure.http.adapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.optimetric.github.application.port.out.BranchClientPort;
import com.optimetric.github.infrastructure.http.dto.GitHubBranchResponse;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.util.GitHubUtil;
import com.optimetric.github.infrastructure.util.UrlBuilder;
import com.optimetric.shared.exception.GitHubApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchClientAdapter implements BranchClientPort {

    private final WebClient githubWebClient;

    @Override
    public List<String> getRepoBranchNames(String accessToken, String ownerLogin, String repoName) {
        final int perPage = 100;
        int page = 1;
        List<String> result = new ArrayList<>();

        while (true) {
            URI uri = UrlBuilder.fromTemplate(
                    GitHubConstants.REPO_BRANCHES_ENDPOINT_TEMPLATE,
                    ownerLogin, repoName, perPage, page
            ).buildUri();

            List<GitHubBranchResponse> pageData = githubWebClient
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
                                    new GitHubApiException(GitHubConstants.ERROR_REPO_FETCH_FAILED + body)))
                    .bodyToFlux(GitHubBranchResponse.class)
                    .collectList()
                    .block();

            if (pageData == null || pageData.isEmpty()) break;

            List<String> branchNames = pageData.stream()
                    .map(GitHubBranchResponse::name)
                    .filter(Objects::nonNull)
                    .filter(name -> !name.isBlank())
                    .toList();
            result.addAll(branchNames);

            if (pageData.size() < perPage) break;
            page++;
        }

        return result;
    }
}

