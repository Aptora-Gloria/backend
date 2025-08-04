package com.oobeya.github.service;

import com.oobeya.github.dto.response.LanguageDto;
import com.oobeya.github.dto.response.LanguageUiDto;

public interface LanguageServiceI {
    LanguageDto getRepoLanguages(String authHeader, String owner, String repo);
    LanguageDto getLanguagesFromDb(String repo);
    LanguageUiDto getLanguagesForUi(String repo);
}
