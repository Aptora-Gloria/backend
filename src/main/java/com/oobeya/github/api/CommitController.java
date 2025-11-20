package com.oobeya.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oobeya.github.api.request.CommitInitRequest;
import com.oobeya.github.api.request.GetCommitsRequest;
import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.application.port.CommitServicePort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/github/commits")
@RequiredArgsConstructor
public class CommitController {
    
    private final CommitServicePort commitService;

    @PostMapping("/init")
    public ResponseEntity<List<CommitDto>> initCommits(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid CommitInitRequest request
    ) {
        List<CommitDto> commits = commitService.initCommits(
                login,
                request.fullName(),
                request.branchName(),
                request.since()
        );
        return ResponseEntity.ok(commits);
    }

    @PostMapping("/query")
    public ResponseEntity<List<CommitDto>> getCommits(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetCommitsRequest request
    ) {
        List<CommitDto> commits = commitService.getCommits(
                login,
                request.fullName(),
                request.branchName()
        );
        return ResponseEntity.ok(commits);
    }
}
