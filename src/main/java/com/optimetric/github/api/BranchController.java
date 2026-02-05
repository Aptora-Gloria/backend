package com.optimetric.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.optimetric.github.api.mapper.BranchApiMapper;
import com.optimetric.github.api.response.BranchDto;
import com.optimetric.github.application.port.in.GetBranchesPort;
import com.optimetric.github.application.port.in.InitBranchesPort;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/github/branches")
@RequiredArgsConstructor
public class BranchController {

    private final InitBranchesPort initBranchesPort;
    private final GetBranchesPort getBranchesPort;

    @PostMapping("/init")
    public ResponseEntity<List<BranchDto>> initBranches(
            @AuthenticationPrincipal String login,
            @RequestParam("fullName") String fullName
    ) {
        var list = initBranchesPort.initBranches(login, fullName);
        return ResponseEntity.ok(BranchApiMapper.toBranchDtos(list));
    }

    @GetMapping("/query")
    public ResponseEntity<List<BranchDto>> getBranches(
            @AuthenticationPrincipal String login,
            @RequestParam("fullName") String fullName
    ) {
        var list = getBranchesPort.getBranches(login, fullName);
        return ResponseEntity.ok(BranchApiMapper.toBranchDtos(list));
    }
}
