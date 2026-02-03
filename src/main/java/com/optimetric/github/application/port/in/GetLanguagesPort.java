package com.optimetric.github.application.port.in;

import com.optimetric.github.application.dto.LanguageInfo;

/**
 * Inbound port: get languages for a repository (from DB).
 */
public interface GetLanguagesPort {

    LanguageInfo getLanguages(String repoFullName);
}
