package com.oobeya.github.application.port;

import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.response.LanguageUiDto;
import com.oobeya.github.dto.request.LanguageSyncRequest;

public interface LanguageServicePort {
    
    /**
     * Synchronizes languages from GitHub API and saves them to database.
     * 
     * @param request Language sync request containing repository info and auth header
     * @return LanguageDto from GitHub API
     */
    LanguageDto syncLanguages(LanguageSyncRequest request);
    
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
