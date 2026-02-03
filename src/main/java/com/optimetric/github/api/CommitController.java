package com.optimetric.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.mapper.CommitApiMapper;
import com.optimetric.github.api.request.CommitInitRequest;
import com.optimetric.github.api.request.GetCommitsRequest;
import com.optimetric.github.api.response.CommitDto;
import com.optimetric.github.application.port.in.GetCommitsPort;
import com.optimetric.github.application.port.in.InitCommitsPort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * API sadece al/ver: Port/In (use case) çağırır, Application DTO'yu ApiMapper ile API DTO'ya çevirir.
 */
@RestController
@RequestMapping("/api/github/commits")
@RequiredArgsConstructor
public class CommitController {

    private final InitCommitsPort initCommitsPort;
    private final GetCommitsPort getCommitsPort;

    @PostMapping("/init")
    public ResponseEntity<List<CommitDto>> initCommits(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid CommitInitRequest request
    ) {
        var list = initCommitsPort.initCommits(
                login,
                request.fullName(),
                request.branchName(),
                request.since()
        );
        return ResponseEntity.ok(CommitApiMapper.toCommitDtos(list));
    }

    @PostMapping("/query")
    public ResponseEntity<List<CommitDto>> getCommits(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetCommitsRequest request
    ) {
        var list = getCommitsPort.getCommits(
                login,
                request.fullName(),
                request.branchName()
        );
        return ResponseEntity.ok(CommitApiMapper.toCommitDtos(list));
    }
}
