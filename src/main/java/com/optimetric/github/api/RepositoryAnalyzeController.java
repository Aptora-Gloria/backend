package com.optimetric.github.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.request.RepositoryAnalyzeRequest;
import com.optimetric.github.api.response.RepositoryAnalyzeResponse;
import com.optimetric.github.application.port.RepositoryAnalyzeServicePort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for repository analyze endpoints.
 * Provides endpoints to analyze repository data readiness and enqueue sync jobs.
 */
@RestController
@RequestMapping("/api/github/repositories")
@RequiredArgsConstructor
public class RepositoryAnalyzeController {

    private final RepositoryAnalyzeServicePort repositoryAnalyzeService;

    /**
     * Analyzes repository data readiness and enqueues sync jobs for missing components.
     * 
     * @param login Authenticated user login (for security)
     * @param request Request containing repository full name
     * @return RepositoryAnalyzeResponse with state, enqueued jobs, and status endpoint
     */
    @PostMapping("/analyze")
    public ResponseEntity<RepositoryAnalyzeResponse> analyze(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid RepositoryAnalyzeRequest request
    ) {
        RepositoryAnalyzeResponse response = repositoryAnalyzeService.analyze(login, request.fullName());
        return ResponseEntity.ok(response);
    }
}
