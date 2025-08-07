package com.oobeya.github.controller;

import com.oobeya.auth.service.AuthService;
import com.oobeya.github.entity.OrganizationEntity;
import com.oobeya.github.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github/orgs")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final AuthService authService;

    @PostMapping("/init")
    public ResponseEntity<List<OrganizationEntity>> initOrganizations(
            @RequestHeader("Authorization") String authHeader
    ) {
        String accessToken = authService.getAccessTokenFromToken(authHeader);
        List<OrganizationEntity> savedOrgs = organizationService.initOrganizations(accessToken);
        return ResponseEntity.ok(savedOrgs);
    }
}
