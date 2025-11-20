package com.oobeya.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oobeya.github.api.response.BranchDto;
import com.oobeya.github.application.port.BranchServicePort;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/github/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchServicePort branchService;

    @PostMapping("/init")
    public ResponseEntity<List<BranchDto>> initBranches(
            @AuthenticationPrincipal String login,
            @RequestParam("fullName") String fullName
    ) {
        return ResponseEntity.ok(
                branchService.initRepoBranches(login, fullName)
        );
    }

    @GetMapping("/query")
    public ResponseEntity<List<BranchDto>> getBranches(
            @AuthenticationPrincipal String login,
            @RequestParam("fullName") String fullName
    ) {
        return ResponseEntity.ok(
                branchService.getBranchesByFullName(login, fullName)
        );
    }
}
