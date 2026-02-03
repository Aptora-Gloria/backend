package com.optimetric.github.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.mapper.RepoSyncStatusApiMapper;
import com.optimetric.github.api.request.RepositorySyncStatusRequest;
import com.optimetric.github.api.response.RepositoryReadinessDto;
import com.optimetric.github.application.port.in.GetRepoSyncStatusPort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * API sadece al/ver: Port/In (use case) çağırır, Application DTO'yu ApiMapper ile API DTO'ya çevirir.
 */
@RestController
@RequestMapping("/api/github/repositories")
@RequiredArgsConstructor
public class RepositorySyncStatusController {

    private final GetRepoSyncStatusPort getRepoSyncStatusPort;

    @PostMapping("/sync/status")
    public ResponseEntity<RepositoryReadinessDto> getSyncStatus(
            @RequestBody @Valid RepositorySyncStatusRequest request
    ) {
        var info = getRepoSyncStatusPort.getReadiness(request.fullName());
        return ResponseEntity.ok(RepoSyncStatusApiMapper.toRepositoryReadinessDto(info));
    }
}
