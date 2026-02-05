package com.optimetric.metrics.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.metrics.api.mapper.UserMetricsApiMapper;
import com.optimetric.metrics.api.request.GetUserMetricsRequest;
import com.optimetric.metrics.api.response.ActivityTrendDto;
import com.optimetric.metrics.api.response.CommitTimeAnalysisDto;
import com.optimetric.metrics.api.response.LanguageDiversityDto;
import com.optimetric.metrics.api.response.PrMergeRateDto;
import com.optimetric.metrics.api.response.PrSizeDto;
import com.optimetric.metrics.api.response.ReviewStatsDto;
import com.optimetric.metrics.api.response.UserMetricsDto;
import com.optimetric.metrics.application.port.UserMetricsServicePort;
import com.optimetric.metrics.application.port.in.GetPrMergeRatePort;
import com.optimetric.metrics.application.port.in.GetActivityTrendPort;
import com.optimetric.metrics.application.port.in.GetCommitTimeAnalysisPort;
import com.optimetric.metrics.application.port.in.GetLanguageDiversityPort;
import com.optimetric.metrics.application.port.in.GetPrSizePort;
import com.optimetric.metrics.application.port.in.GetReviewStatsPort;
import com.optimetric.shared.exception.UserNotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final UserMetricsServicePort userMetricsService;
    private final GetPrMergeRatePort getPrMergeRatePort;
    private final GetPrSizePort getPrSizePort;
    private final GetReviewStatsPort getReviewStatsPort;
    private final GetCommitTimeAnalysisPort getCommitTimeAnalysisPort;
    private final GetLanguageDiversityPort getLanguageDiversityPort;
    private final GetActivityTrendPort getActivityTrendPort;

    @PostMapping("/user")
    public ResponseEntity<UserMetricsDto> getUserMetrics(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        var info = userMetricsService.getUserMetrics(request.login());
        return ResponseEntity.ok(UserMetricsApiMapper.toUserMetricsDto(info));
    }

    @PostMapping("/pr-merge-rate")
    public ResponseEntity<PrMergeRateDto> getPrMergeRate(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        var info = getPrMergeRatePort.getPrMergeRate(request.login());
        return ResponseEntity.ok(UserMetricsApiMapper.toPrMergeRateDto(info));
    }

    @PostMapping("/pr-size")
    public ResponseEntity<PrSizeDto> getPrSize(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        var info = getPrSizePort.getPrSize(request.login());
        return ResponseEntity.ok(UserMetricsApiMapper.toPrSizeDto(info));
    }

    @PostMapping("/review-stats")
    public ResponseEntity<ReviewStatsDto> getReviewStats(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        var info = getReviewStatsPort.getReviewStats(request.login());
        return ResponseEntity.ok(UserMetricsApiMapper.toReviewStatsDto(info));
    }

    @PostMapping("/commit-time-analysis")
    public ResponseEntity<CommitTimeAnalysisDto> getCommitTimeAnalysis(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        var info = getCommitTimeAnalysisPort.getCommitTimeAnalysis(request.login());
        return ResponseEntity.ok(UserMetricsApiMapper.toCommitTimeAnalysisDto(info));
    }

    @PostMapping("/language-diversity")
    public ResponseEntity<LanguageDiversityDto> getLanguageDiversity(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        var info = getLanguageDiversityPort.getLanguageDiversity(request.login());
        return ResponseEntity.ok(UserMetricsApiMapper.toLanguageDiversityDto(info));
    }

    @PostMapping("/activity-trend")
    public ResponseEntity<ActivityTrendDto> getActivityTrend(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetUserMetricsRequest request
    ) throws UserNotFoundException {
        var info = getActivityTrendPort.getActivityTrend(request.login());
        return ResponseEntity.ok(UserMetricsApiMapper.toActivityTrendDto(info));
    }
}

