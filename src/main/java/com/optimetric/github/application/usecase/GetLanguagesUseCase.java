package com.optimetric.github.application.usecase;

import com.optimetric.github.application.dto.LanguageInfo;
import com.optimetric.github.application.port.in.GetLanguagesPort;
import com.optimetric.github.application.port.out.LanguageQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetLanguagesUseCase implements GetLanguagesPort {

    private final LanguageQueryPort languageQueryPort;

    @Override
    public LanguageInfo getLanguages(String repoFullName) {
        if (repoFullName == null || repoFullName.isBlank()) throw new IllegalArgumentException("repoFullName must not be blank");
        return languageQueryPort.getLanguages(repoFullName);
    }
}
