package com.optimetric.github.application.port;

import com.optimetric.github.api.response.LanguageDto;
import com.optimetric.github.api.response.LanguageUiDto;

public interface LanguageServicePort {
    
    /**
     * Fetches languages from GitHub API and saves them to database.
     * 
     * @param login User login
     * @param repositoryId Repository ID
     * @return LanguageDto from GitHub API
     */
    LanguageDto getRepoLanguages(String login, Long repositoryId);
    
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
