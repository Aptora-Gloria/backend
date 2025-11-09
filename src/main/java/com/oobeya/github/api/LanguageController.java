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
            @RequestBody LanguageInitRequest request) {

        return ResponseEntity.ok(languageService.getRepoLanguages(authHeader, request.owner(), request.repo()));
    }


    @GetMapping("/languages/get")
    public ResponseEntity<LanguageDto> getLanguagesFromDb(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("repo") String repo) {

        return ResponseEntity.ok(languageService.getLanguagesFromDb(repo));
    }


    @GetMapping("/languages/ui")
    public ResponseEntity<LanguageUiDto> getLanguagesForUi(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("repo") String repo) {

        return ResponseEntity.ok(languageService.getLanguagesForUi(repo));
    }
}
