package com.oobeya.github.controller;

import com.oobeya.github.dto.ContentDto;
import com.oobeya.github.dto.ContentInitRequest;
import com.oobeya.github.service.ContentServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github") //VERSİYONlama v1 v2 mi araştır
@RequiredArgsConstructor
public class ContentController {

    private final ContentServiceI contentService;


    @PostMapping("/contents/init")
    public ResponseEntity<List<ContentDto>> initRepoContents(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ContentInitRequest request
    ) {
        List<ContentDto> contents = contentService.fectAndSaveContent(
                authHeader,
                request.owner(),
                request.repo(),
                request.path()
        );
        return ResponseEntity.ok(contents);
    }


    @GetMapping("/contents")
    public ResponseEntity<List<ContentDto>> getRepoContents(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("owner") String owner,
            @RequestParam("repo") String repo,
            @RequestParam(value = "path", required = false, defaultValue = "") String path) {


        List<ContentDto> contents = contentService.getContentFromDb(owner, repo);
        return ResponseEntity.ok(contents);
    }
}
