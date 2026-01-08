package com.optimetric.github.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.request.GetLanguagesRequest;
import com.optimetric.github.api.request.LanguageInitRequest;
import com.optimetric.github.api.response.LanguageDto;
import com.optimetric.github.api.response.LanguageUiDto;
import com.optimetric.github.application.port.LanguageServicePort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageServicePort languageService;

    @PostMapping("/languages/init")
    public ResponseEntity<LanguageDto> initLanguages(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid LanguageInitRequest request
    ) {
        LanguageDto dto = languageService.initRepoLanguages(login, request.repoFullName());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/languages/query")
    public ResponseEntity<LanguageDto> getLanguagesFromDb(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetLanguagesRequest request
    ) {
        LanguageDto dto = languageService.getLanguagesFromDb(request.repoFullName());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/languages/query/ui")
    public ResponseEntity<LanguageUiDto> getLanguagesForUi(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetLanguagesRequest request
    ) {
        LanguageUiDto uiDto = languageService.getLanguagesForUi(request.repoFullName());
        return ResponseEntity.ok(uiDto);
    }
}
