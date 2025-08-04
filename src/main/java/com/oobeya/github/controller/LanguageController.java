package com.oobeya.github.controller;

import com.oobeya.github.dto.response.LanguageDto;
import com.oobeya.github.dto.request.LanguageInitRequest;
import com.oobeya.github.dto.response.LanguageUiDto;
import com.oobeya.github.service.LanguageServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageServiceI languageService;


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
