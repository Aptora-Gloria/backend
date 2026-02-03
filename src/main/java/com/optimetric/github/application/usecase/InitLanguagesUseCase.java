package com.optimetric.github.application.usecase;

import com.optimetric.github.application.dto.LanguageInfo;
import com.optimetric.github.application.port.in.InitLanguagesPort;
import com.optimetric.github.application.port.out.LanguageQueryPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case: initialize languages for a repository from GitHub.
 */
@Service
@RequiredArgsConstructor
public class InitLanguagesUseCase implements InitLanguagesPort {

    private final LanguageQueryPort languageQueryPort;

    @Override
    public LanguageInfo initLanguages(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("login must not be blank");
        if (repoFullName == null || repoFullName.isBlank()) throw new IllegalArgumentException("repoFullName must not be blank");
        return languageQueryPort.initRepoLanguages(login, repoFullName);
    }
}
