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
     * @param repositoryId Repository ID
     * @return LanguageDto from GitHub API
     * @throws RepositoryNotFoundException if repository not found
     * @throws UserNotFoundException if user not found
     */
    LanguageDto initRepoLanguages(String login, Long repositoryId)
            throws RepositoryNotFoundException, UserNotFoundException;
    
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
