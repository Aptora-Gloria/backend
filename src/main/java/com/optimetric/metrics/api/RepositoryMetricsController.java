package com.optimetric.metrics.api;

import com.optimetric.metrics.api.mapper.RepositoryMetricsApiMapper;
import com.optimetric.metrics.api.request.GetRepositoryMetricsRequest;
import com.optimetric.metrics.api.response.*;
import com.optimetric.metrics.application.port.RepositoryMetricsServicePort;
import com.optimetric.metrics.application.port.in.GetBranchMetricsPort;
import com.optimetric.metrics.application.port.in.GetCodeQualityPort;
import com.optimetric.metrics.application.port.in.GetCommitMetricsPort;
import com.optimetric.metrics.application.port.in.GetContributorMetricsPort;
import com.optimetric.metrics.application.port.in.GetHealthScorePort;
import com.optimetric.metrics.application.port.in.GetLanguageMetricsPort;
import com.optimetric.metrics.application.port.in.GetOverviewPort;
import com.optimetric.metrics.application.port.in.GetPullRequestMetricsPort;
import com.optimetric.metrics.application.port.in.GetRepositoryActivityTrendPort;
import com.optimetric.metrics.application.port.in.GetReviewMetricsPort;
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
    private final GetOverviewPort getOverviewPort;
    private final GetLanguageMetricsPort getLanguageMetricsPort;
    private final GetHealthScorePort getHealthScorePort;
    private final GetReviewMetricsPort getReviewMetricsPort;
    private final GetBranchMetricsPort getBranchMetricsPort;
    private final GetPullRequestMetricsPort getPullRequestMetricsPort;
    private final GetCommitMetricsPort getCommitMetricsPort;
    private final GetRepositoryActivityTrendPort getRepositoryActivityTrendPort;
    private final GetCodeQualityPort getCodeQualityPort;
    private final GetContributorMetricsPort getContributorMetricsPort;

    @PostMapping
    public ResponseEntity<RepositoryMetricsDto> getRepositoryMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = repositoryMetricsService.getRepositoryMetrics(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toRepositoryMetricsDto(info));
    }

    @PostMapping("/overview")
    public ResponseEntity<RepositoryOverviewDto> getOverview(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = getOverviewPort.getOverview(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toOverviewDto(info));
    }

    @PostMapping("/pull-requests")
    public ResponseEntity<RepositoryPrMetricsDto> getPullRequestMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = getPullRequestMetricsPort.getPullRequestMetrics(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toPrMetricsDto(info));
    }

    @PostMapping("/commits")
    public ResponseEntity<RepositoryCommitMetricsDto> getCommitMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = getCommitMetricsPort.getCommitMetrics(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toCommitMetricsDto(info));
    }

    @PostMapping("/reviews")
    public ResponseEntity<RepositoryReviewMetricsDto> getReviewMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = getReviewMetricsPort.getReviewMetrics(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toReviewMetricsDto(info));
    }

    @PostMapping("/branches")
    public ResponseEntity<RepositoryBranchMetricsDto> getBranchMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = getBranchMetricsPort.getBranchMetrics(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toBranchMetricsDto(info));
    }

    @PostMapping("/languages")
    public ResponseEntity<RepositoryLanguageMetricsDto> getLanguageMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = getLanguageMetricsPort.getLanguageMetrics(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toLanguageMetricsDto(info));
    }

    @PostMapping("/activity")
    public ResponseEntity<RepositoryActivityTrendDto> getActivityTrend(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = getRepositoryActivityTrendPort.getActivityTrend(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toActivityTrendDto(info));
    }

    @PostMapping("/code-quality")
    public ResponseEntity<RepositoryCodeQualityDto> getCodeQuality(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = getCodeQualityPort.getCodeQuality(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toCodeQualityDto(info));
    }

    @PostMapping("/contributors")
    public ResponseEntity<RepositoryContributorMetricsDto> getContributorMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = getContributorMetricsPort.getContributorMetrics(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toContributorMetricsDto(info));
    }

    @PostMapping("/health")
    public ResponseEntity<RepositoryHealthScoreDto> getHealthScore(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetRepositoryMetricsRequest request
    ) throws RepositoryNotFoundException, UserNotFoundException {
        var info = getHealthScorePort.getHealthScore(login, request.repoFullName());
        return ResponseEntity.ok(RepositoryMetricsApiMapper.toHealthScoreDto(info));
    }
}

