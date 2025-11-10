package com.oobeya.github.application.port;

import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.response.LanguageUiDto;

public interface LanguageServicePort {
    
    /**
     * Fetches languages from GitHub API and saves them to database.
     * 
     * @param authHeader Authorization header
     * @param repositoryId Repository ID
     * @return LanguageDto from GitHub API
     */
    LanguageDto getRepoLanguages(String authHeader, Long repositoryId);
    
    /**
     * Gets languages from database by repository ID (raw Map format).
     * 
     * @param repositoryId Repository ID
     * @return LanguageDto
     */
    LanguageDto getLanguagesFromDb(Long repositoryId);
    
    /**
     * Gets languages from database by repository ID (UI format with percentages).
     * 
     * @param repositoryId Repository ID
     * @return LanguageUiDto
     */
    LanguageUiDto getLanguagesForUi(Long repositoryId);
}
