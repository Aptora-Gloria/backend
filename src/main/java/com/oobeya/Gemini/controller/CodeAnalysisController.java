// src/main/java/com/oobeya/Gemini/controller/CodeAnalysisController.java
package com.oobeya.Gemini.controller;

import com.oobeya.Gemini.dto.FileAnalysisRequest;
import com.oobeya.Gemini.service.CodeAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini/analysis")
@RequiredArgsConstructor
public class CodeAnalysisController {
    private final CodeAnalysisService codeAnalysisService;

    @PostMapping("/file/recommendations")
    public ResponseEntity<String> analyzeFile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody FileAnalysisRequest request) {
        String recommendations = codeAnalysisService.analyzeFileAndGetRecommendations(authHeader,
                request.getOwner(), request.getRepoName(), request.getFilePath());
        return ResponseEntity.ok(recommendations);
    }
}