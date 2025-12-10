package com.optimetric.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.api.response.SyncMetricsResponse;
import com.optimetric.github.application.port.PullRequestServicePort;
import com.optimetric.github.application.service.PullRequestMetricsSyncService;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/github/pull-requests")
@RequiredArgsConstructor
public class PullRequestController {

    private final PullRequestServicePort pullRequestService;
    private final PullRequestMetricsSyncService metricsSyncService;

    @PostMapping("/init")
    public ResponseEntity<List<PullRequestDto>> initPullRequests(
            @AuthenticationPrincipal String login,
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "state", required = false, defaultValue = "all") String state
    ) {
        return ResponseEntity.ok(
                pullRequestService.initPullRequests(login, fullName, state)
        );
    }

    @GetMapping("/query")
    public ResponseEntity<List<PullRequestDto>> getPullRequests(
            @AuthenticationPrincipal String login,
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "state", required = false) String state
    ) {
        return ResponseEntity.ok(
                pullRequestService.getPullRequestsByFullName(login, fullName, state)
        );
    }

    @PostMapping("/sync-metrics")
    public ResponseEntity<SyncMetricsResponse> syncMetrics(
            @AuthenticationPrincipal String login,
            @RequestParam("fullName") String fullName
    ) throws RepositoryNotFoundException, UserNotFoundException {
        SyncMetricsResponse response = metricsSyncService.syncMetricsForRepository(login, fullName);
        return ResponseEntity.ok(response);
    }
}


