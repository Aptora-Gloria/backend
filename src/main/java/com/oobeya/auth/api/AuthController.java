package com.oobeya.auth.api;

import com.oobeya.auth.api.dto.AuthRequest;
import com.oobeya.auth.api.dto.AuthResponse;
import com.oobeya.auth.api.dto.GitHubUserDto;
import com.oobeya.auth.application.AuthServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServicePort authServicePort;

    // 1. Giriş (login) endpoint'i
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authServicePort.login(request);

        return ResponseEntity.ok(response);
    }

    // 2. Me (kendi bilgini getir) endpoint'i
    @GetMapping("/me")
    public ResponseEntity<GitHubUserDto> me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authorizationHeader.replace("Bearer ", "").trim();
        if (token.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        GitHubUserDto user = authServicePort.getCurrentUser(token);
        return ResponseEntity.ok(user);
    }
}
