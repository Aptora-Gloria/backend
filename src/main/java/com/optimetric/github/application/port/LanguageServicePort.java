package com.optimetric.github.application.port;

import com.optimetric.github.api.response.LanguageDto;
import com.optimetric.github.api.response.LanguageUiDto;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface LanguageServicePort {
    
    /**
     * Fetches languages from GitHub API and saves them to database.
     * 
     * @param login User login
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return LanguageDto from GitHub API
     * @throws RepositoryNotFoundException if repository not found
     * @throws UserNotFoundException if user not found
     */
    LanguageDto initRepoLanguages(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;
    
    /**
     * Gets languages from database by repository full name (raw Map format).
     * 
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return LanguageDto
     */
    LanguageDto getLanguagesFromDb(String repoFullName);
    
    /**
     * Gets languages from database by repository full name (UI format with percentages).
     * 
     * @param repoFullName Repository full name (e.g., "owner/repo")
     * @return LanguageUiDto
     */
    LanguageUiDto getLanguagesForUi(String repoFullName);
}
