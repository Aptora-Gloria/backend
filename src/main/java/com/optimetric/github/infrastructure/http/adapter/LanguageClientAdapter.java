package com.optimetric.github.infrastructure.http.adapter;

import com.optimetric.github.application.dto.LanguageInfo;
import com.optimetric.github.application.port.out.LanguageClientPort;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LanguageClientAdapter implements LanguageClientPort {

    private final WebClient githubWebClient;

    @Override
    public LanguageInfo getRepoLanguages(String accessToken, String owner, String repo) {
        URI uri = UrlBuilder.fromTemplate(
                GitHubConstants.REPO_LANGUAGES_ENDPOINT_TEMPLATE, owner, repo
        ).buildUri();

        @SuppressWarnings("unchecked")
        Map<String, Object> languages = githubWebClient
                .get()
                .uri(uri)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, GitHubUtil.buildBearerHeader(accessToken)))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).map(body ->
                                new GitHubApiException("Failed to fetch languages: " + body)))
                .bodyToMono(Map.class)
                .block();

        if (languages == null || languages.isEmpty()) {
            return new LanguageInfo(Map.of());
        }

        Map<String, Long> casted = languages.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> ((Number) e.getValue()).longValue()
                ));

        return new LanguageInfo(casted);
    }
}

