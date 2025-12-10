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
import com.optimetric.github.application.port.PullRequestServicePort;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/github/pull-requests")
@RequiredArgsConstructor
public class PullRequestController {

    private final PullRequestServicePort pullRequestService;

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
}


