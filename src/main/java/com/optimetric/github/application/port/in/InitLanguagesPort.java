package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.LanguageInfo;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

public interface InitLanguagesPort {

    LanguageInfo initLanguages(String login, String repoFullName)
            throws UserNotFoundException, RepositoryNotFoundException;
}
