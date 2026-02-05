package com.optimetric.github.application.port.out;

import com.optimetric.github.application.dto.LanguageInfo;

public interface LanguageClientPort {
    LanguageInfo getRepoLanguages(String accessToken, String owner, String repo);
}
