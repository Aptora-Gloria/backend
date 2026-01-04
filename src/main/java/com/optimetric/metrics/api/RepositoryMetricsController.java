package com.optimetric.metrics.api;

import com.optimetric.metrics.api.request.GetRepositoryMetricsRequest;
import com.optimetric.metrics.api.response.*;
import com.optimetric.metrics.application.port.RepositoryMetricsServicePort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics/repository")
@RequiredArgsConstructor
public class RepositoryMetricsController {

    private final RepositoryMetricsServicePort repositoryMetricsService;

    @PostMapping
    public ResponseEntity<RepositoryMetricsDto> getRepositoryMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryMetricsDto metrics = repositoryMetricsService.getRepositoryMetrics(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/overview")
    public ResponseEntity<RepositoryOverviewDto> getOverview(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryOverviewDto metrics = repositoryMetricsService.getOverview(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/pull-requests")
    public ResponseEntity<RepositoryPrMetricsDto> getPullRequestMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryPrMetricsDto metrics = repositoryMetricsService.getPullRequestMetrics(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/commits")
    public ResponseEntity<RepositoryCommitMetricsDto> getCommitMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryCommitMetricsDto metrics = repositoryMetricsService.getCommitMetrics(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/reviews")
    public ResponseEntity<RepositoryReviewMetricsDto> getReviewMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryReviewMetricsDto metrics = repositoryMetricsService.getReviewMetrics(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/branches")
    public ResponseEntity<RepositoryBranchMetricsDto> getBranchMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryBranchMetricsDto metrics = repositoryMetricsService.getBranchMetrics(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/languages")
    public ResponseEntity<RepositoryLanguageMetricsDto> getLanguageMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryLanguageMetricsDto metrics = repositoryMetricsService.getLanguageMetrics(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/activity")
    public ResponseEntity<RepositoryActivityTrendDto> getActivityTrend(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryActivityTrendDto metrics = repositoryMetricsService.getActivityTrend(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/code-quality")
    public ResponseEntity<RepositoryCodeQualityDto> getCodeQuality(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryCodeQualityDto metrics = repositoryMetricsService.getCodeQuality(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/contributors")
    public ResponseEntity<RepositoryContributorMetricsDto> getContributorMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryContributorMetricsDto metrics = repositoryMetricsService.getContributorMetrics(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/health")
    public ResponseEntity<RepositoryHealthScoreDto> getHealthScore(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        RepositoryHealthScoreDto metrics = repositoryMetricsService.getHealthScore(
                login, request.repoFullName());
        return ResponseEntity.ok(metrics);
    }
}

