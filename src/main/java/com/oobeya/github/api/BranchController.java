package com.oobeya.github.api;

import com.oobeya.github.api.request.BranchInitRequest;
import com.oobeya.github.api.response.BranchDto;
import com.oobeya.github.application.port.BranchServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchServicePort branchService;

    @PostMapping("/init")
    public ResponseEntity<List<BranchDto>> initBranches(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BranchInitRequest request
    ) {
        return ResponseEntity.ok(
                branchService.initRepoBranches(authHeader, request.fullName())
        );
    }

    @GetMapping
    public ResponseEntity<List<BranchDto>> getBranches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("fullName") String fullName
    ) {
        return ResponseEntity.ok(
                branchService.getBranchesByFullName(authHeader, fullName)
        );
    }
}
