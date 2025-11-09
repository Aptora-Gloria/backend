package com.oobeya.github.api;


import com.oobeya.github.api.response.CommitDto;
import com.oobeya.github.api.request.CommitInitRequest;
import com.oobeya.github.application.port.CommitServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github") //VERSİYONlama v1 v2 mi araştır
@RequiredArgsConstructor
public class CommitController {
    private final CommitServicePort commitService;

    @PostMapping("/commits/init")
    public ResponseEntity<List<CommitDto>> initUserReposCommit(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CommitInitRequest request) {

        List<CommitDto> commits = commitService.fecthAndSaveCommits(
                authHeader,
                request.owner(),
                request.repo()
        );

        return ResponseEntity.ok(commits);
    }


    @GetMapping("/commits/get")
    public ResponseEntity<List<CommitDto>> getCommits(// path variblle araştır
            @RequestHeader("Authorization") String authHeader,//TODO jwt filter ile yapılcak
            @RequestParam("owner") String owner,
            @RequestParam("repo") String repo) {


        List<CommitDto> commits  = commitService.getCommitsFromDb(authHeader,repo);
        return ResponseEntity.ok(commits);
    }
}
