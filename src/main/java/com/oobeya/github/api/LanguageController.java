package com.oobeya.github.api;

import com.oobeya.github.api.response.LanguageDto;
import com.oobeya.github.api.request.LanguageInitRequest;
import com.oobeya.github.api.response.LanguageUiDto;
import com.oobeya.github.application.port.LanguageServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageServicePort languageService;

    @PostMapping("/languages/init")
    public ResponseEntity<LanguageDto> initLanguages(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LanguageInitRequest request
    ) {
        LanguageDto dto = languageService.getRepoLanguages(authHeader, request.repositoryId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/languages/repository/{repositoryId}")
    public ResponseEntity<LanguageDto> getLanguagesFromDb(
            @PathVariable("repositoryId") Long repositoryId
    ) {
        LanguageDto dto = languageService.getLanguagesFromDb(repositoryId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/languages/repository/{repositoryId}/ui")
    public ResponseEntity<LanguageUiDto> getLanguagesForUi(
            @PathVariable("repositoryId") Long repositoryId
    ) {
        LanguageUiDto uiDto = languageService.getLanguagesForUi(repositoryId);
        return ResponseEntity.ok(uiDto);
    }
}
