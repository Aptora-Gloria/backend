package com.optimetric.metrics.api;

import com.optimetric.metrics.api.request.GetUserMetricsRequest;
import com.optimetric.metrics.api.response.*;
import com.optimetric.metrics.application.port.UserMetricsServicePort;
import com.optimetric.shared.exception.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final UserMetricsServicePort userMetricsService;

    @PostMapping("/user")
    public ResponseEntity<UserMetricsDto> getUserMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        UserMetricsDto metrics = userMetricsService.getUserMetrics(request.login());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/pr-merge-rate")
    public ResponseEntity<PrMergeRateDto> getPrMergeRate(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        PrMergeRateDto metrics = userMetricsService.getPrMergeRate(request.login());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/pr-size")
    public ResponseEntity<PrSizeDto> getPrSize(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        PrSizeDto metrics = userMetricsService.getPrSize(request.login());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/review-stats")
    public ResponseEntity<ReviewStatsDto> getReviewStats(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        ReviewStatsDto metrics = userMetricsService.getReviewStats(request.login());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/commit-time-analysis")
    public ResponseEntity<CommitTimeAnalysisDto> getCommitTimeAnalysis(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        CommitTimeAnalysisDto metrics = userMetricsService.getCommitTimeAnalysis(request.login());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/language-diversity")
    public ResponseEntity<LanguageDiversityDto> getLanguageDiversity(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        LanguageDiversityDto metrics = userMetricsService.getLanguageDiversity(request.login());
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/activity-trend")
    public ResponseEntity<ActivityTrendDto> getActivityTrend(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        ActivityTrendDto metrics = userMetricsService.getActivityTrend(request.login());
        return ResponseEntity.ok(metrics);
    }
}

