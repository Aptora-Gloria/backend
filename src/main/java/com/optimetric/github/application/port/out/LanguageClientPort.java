package com.optimetric.github.application.port.out;

import com.optimetric.github.api.response.LanguageDto;

/**
 * Outbound port for fetching repository languages from GitHub API.
 */
public interface LanguageClientPort {
    LanguageDto getRepoLanguages(String accessToken, String owner, String repo);
}
