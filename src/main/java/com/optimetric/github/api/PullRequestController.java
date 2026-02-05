package com.optimetric.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.mapper.PullRequestApiMapper;
import com.optimetric.github.api.request.GetPullRequestRequest;
import com.optimetric.github.api.request.GetPullRequestsRequest;
import com.optimetric.github.api.request.GetSyncStatusRequest;
import com.optimetric.github.api.request.InitiatePullRequestSyncRequest;
import com.optimetric.github.api.response.PullRequestDto;
import com.optimetric.github.api.response.ReviewCommentDto;
import com.optimetric.github.api.response.ReviewDto;
import com.optimetric.github.api.response.SyncStatusDto;
import com.optimetric.github.application.port.in.GetPullRequestCommitsPort;
import com.optimetric.github.application.port.in.GetPullRequestPort;
import com.optimetric.github.application.port.in.GetPullRequestReviewsPort;
import com.optimetric.github.application.port.in.GetPullRequestsPort;
import com.optimetric.github.application.port.in.GetSyncStatusPort;
import com.optimetric.github.application.port.in.InitiatePullRequestSyncPort;
import com.optimetric.shared.exception.RepositoryNotFoundException;
import com.optimetric.shared.exception.UserNotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Pull request işlemleri REST controller; port çağırır, ApiMapper ile DTO döner.
@RestController
@RequestMapping("/api/github/pull-requests")
@RequiredArgsConstructor
public class PullRequestController {

    private final GetPullRequestsPort getPullRequestsPort;
    private final GetPullRequestPort getPullRequestPort;
    private final GetPullRequestReviewsPort getPullRequestReviewsPort;
    private final GetPullRequestCommitsPort getPullRequestCommitsPort;
    private final InitiatePullRequestSyncPort initiatePullRequestSyncPort;
    private final GetSyncStatusPort getSyncStatusPort;

    // Belirtilen repo için tüm PR'ları döner (data level ve sync durumu ile).
    @PostMapping("/query")
    public ResponseEntity<List<PullRequestDto>> getPullRequests(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetPullRequestsRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        var list = getPullRequestsPort.getPullRequestsForRepository(login, request.repoFullName());
        return ResponseEntity.ok(PullRequestApiMapper.toPullRequestDtos(list));
    }

    // Repo ve PR numarasıyla tekil PR döner.
    @PostMapping("/query/single")
    public ResponseEntity<PullRequestDto> getPullRequest(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetPullRequestRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        var info = getPullRequestPort.getPullRequest(login, request.repoFullName(), request.prNumber());
        return ResponseEntity.ok(PullRequestApiMapper.toPullRequestDto(info));
    }

    // Belirtilen PR için tüm incelemeleri (review) döner.
    @PostMapping("/reviews")
    public ResponseEntity<List<ReviewDto>> getPullRequestReviews(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetPullRequestRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        var list = getPullRequestReviewsPort.getPullRequestReviews(login, request.repoFullName(), request.prNumber());
        return ResponseEntity.ok(PullRequestApiMapper.toReviewDtos(list));
    }

    // Belirtilen PR için tüm commit'leri döner (ReviewCommentDto olarak).
    @PostMapping("/commits")
    public ResponseEntity<List<ReviewCommentDto>> getPullRequestCommits(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetPullRequestRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        var list = getPullRequestCommitsPort.getPullRequestCommits(login, request.repoFullName(), request.prNumber());
        return ResponseEntity.ok(PullRequestApiMapper.toReviewCommentDtos(list));
    }

    // Repo için PR senkronizasyonunu başlatır; GitHub API'den veri toplar.
    @PostMapping("/sync/initiate")
    public ResponseEntity<Void> initiatePullRequestSync(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid InitiatePullRequestSyncRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        initiatePullRequestSyncPort.initiatePullRequestSync(login, request.repoFullName());
        return ResponseEntity.ok().build();
    }

    // Repo için sync durumunu döner; devam eden ve tamamlanan job bilgisi.
    @PostMapping("/sync/status")
    public ResponseEntity<SyncStatusDto> getSyncStatus(
            @AuthenticationPrincipal String login,
            @RequestBody @Valid GetSyncStatusRequest request
    ) throws UserNotFoundException, RepositoryNotFoundException {
        var info = getSyncStatusPort.getSyncStatus(login, request.repoFullName());
        return ResponseEntity.ok(PullRequestApiMapper.toSyncStatusDto(info));
    }
}