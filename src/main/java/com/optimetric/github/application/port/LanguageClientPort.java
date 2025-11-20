package com.optimetric.github.application.port;

import com.optimetric.github.api.response.LanguageDto;

public interface LanguageClientPort {
    LanguageDto getRepoLanguages(String accessToken, String owner, String repo);
}

