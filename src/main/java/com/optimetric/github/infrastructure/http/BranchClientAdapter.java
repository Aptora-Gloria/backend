package com.optimetric.github.infrastructure.http;

import com.optimetric.github.api.response.BranchDto;
import com.optimetric.github.application.client.BranchClientPort;
import com.optimetric.github.infrastructure.core.constant.GitHubConstants;
import com.optimetric.github.infrastructure.util.GitHubUtil;
import com.optimetric.shared.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

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
}

