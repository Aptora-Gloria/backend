package com.oobeya.auth.controller;

import com.oobeya.auth.dto.AuthRequest;
import com.oobeya.auth.dto.AuthResponse;
import com.oobeya.auth.dto.GitHubUserDto;
import com.oobeya.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. Giriş (login) endpoint'i
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    // 2. Me (kendi bilgini getir) endpoint'i
    @GetMapping("/me")
    public ResponseEntity<GitHubUserDto> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "").trim();
        GitHubUserDto user = authService.getCurrentUser(token);
        return ResponseEntity.ok(user);
    }
}
