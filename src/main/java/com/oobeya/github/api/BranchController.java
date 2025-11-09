package com.oobeya.github.api;


import com.oobeya.github.api.response.BranchDto;
import com.oobeya.github.application.port.BranchServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class BranchController {

    private final BranchServicePort branchService;

    @PostMapping("/branches/init")
    public ResponseEntity<List<BranchDto>> initBranches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("repoHtmlUrl") String repoHtmlUrl
    ) {
        return ResponseEntity.ok(
                branchService.initRepoBranches(authHeader, repoHtmlUrl)
        );
    }


    @GetMapping("/branches")
    public ResponseEntity<List<BranchDto>> getBranches(
            @RequestParam("repoHtmlUrl") String repoHtmlUrl
    ) {
        return ResponseEntity.ok(
                branchService.getBranchesFromDb(repoHtmlUrl)
        );
    }
}
