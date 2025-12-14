package com.oobeya.github.api;

import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.response.LanguageUiDto;
import com.oobeya.github.application.port.LanguageServicePort;
import com.oobeya.github.dto.request.LanguageSyncRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageServicePort languageService;

    /**
     * Synchronizes languages from GitHub API and saves them to database.
     * Frontend should provide: repositoryId, owner, repositoryName, and authHeader.
     */
    @PostMapping("/languages/sync")
    public ResponseEntity<LanguageDto> syncLanguages(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody LanguageSyncRequest request
    ) {
        // Set authHeader from request header
        LanguageSyncRequest requestWithAuth = new LanguageSyncRequest(
                request.repositoryId(),
                request.owner(),
                request.repositoryName(),
                request.syncSince(),
                authHeader
        );
        LanguageDto dto = languageService.syncLanguages(requestWithAuth);
        return ResponseEntity.ok(dto);
    }

    /**
     * Gets languages from database by repository ID (raw Map format).
     */
    @GetMapping("/languages/repository/{repositoryId}")
    public ResponseEntity<LanguageDto> getLanguagesFromDb(
            @PathVariable("repositoryId") Long repositoryId
    ) {
        LanguageDto dto = languageService.getLanguagesFromDb(repositoryId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Gets languages from database by repository ID (UI format with percentages).
     * This is the preferred endpoint for frontend to display language statistics.
     */
    @GetMapping("/languages/repository/{repositoryId}/ui")
    public ResponseEntity<LanguageUiDto> getLanguagesForUi(
            @PathVariable("repositoryId") Long repositoryId
    ) {
        LanguageUiDto uiDto = languageService.getLanguagesForUi(repositoryId);
        return ResponseEntity.ok(uiDto);
    }
}
