package com.oobeya.github.application.port;

import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.response.LanguageUiDto;

public interface LanguageServicePort {
    LanguageDto getRepoLanguages(String authHeader, String owner, String repo);
    LanguageDto getLanguagesFromDb(String repo);
    LanguageUiDto getLanguagesForUi(String repo);
}
