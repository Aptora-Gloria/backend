package com.optimetric.github.application.port.out;

import com.optimetric.github.application.dto.LanguageInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

/**
 * Outbound port for language operations (init and query).
 */
public interface LanguageQueryPort {

    LanguageInfo initRepoLanguages(String login, String repoFullName)
            throws RepositoryNotFoundException, UserNotFoundException;

    LanguageInfo getLanguages(String repoFullName);
}
