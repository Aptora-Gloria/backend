package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.LanguageInfo;

public interface GetLanguagesPort {

    LanguageInfo getLanguages(String repoFullName);
}
