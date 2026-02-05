package com.optimetric.auth.api;

import com.optimetric.auth.api.dto.AuthRequest;
import com.optimetric.auth.api.dto.AuthResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;
import com.optimetric.auth.application.port.in.GetCurrentUserPort;
import com.optimetric.auth.application.port.in.LoginPort;
import com.optimetric.auth.infrastructure.core.mapper.AuthApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Auth API: giriş ve mevcut kullanıcı endpoint'leri; port/in kullanır.
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginPort loginPort;
    private final GetCurrentUserPort getCurrentUserPort;

    // GitHub OAuth kodu ile giriş; JWT döner.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        var command = AuthApiMapper.toLoginCommand(request);
        var result = loginPort.execute(command);
        return ResponseEntity.ok(AuthApiMapper.toAuthResponse(result));
    }

    // Bearer token ile mevcut kullanıcı bilgisini döner.
    @GetMapping("/me")
    public ResponseEntity<GitHubUserDto> me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authorizationHeader.replace("Bearer ", "").trim();
        if (token.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        var userInfo = getCurrentUserPort.execute(token);
        return ResponseEntity.ok(AuthApiMapper.toGitHubUserDto(userInfo));
    }
}
