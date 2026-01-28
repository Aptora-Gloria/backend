package com.optimetric.auth.api;

import com.optimetric.auth.api.dto.AuthRequest;
import com.optimetric.auth.api.dto.AuthResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;
import com.optimetric.auth.application.usecase.LoginUseCase;
import com.optimetric.auth.application.usecase.GetCurrentUserUseCase;
import com.optimetric.auth.infrastructure.core.mapper.AuthApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    // 1. Giriş (login) endpoint'i
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        // Convert API DTO to Application DTO
        var command = AuthApiMapper.toLoginCommand(request);
        
        // Call use case
        var result = loginUseCase.execute(command);
        
        // Convert Application DTO to API DTO
        var response = AuthApiMapper.toAuthResponse(result);
        
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
        
        // Call use case (returns Application DTO)
        var userInfo = getCurrentUserUseCase.execute(token);
        
        // Convert Application DTO to API DTO
        var userDto = AuthApiMapper.toGitHubUserDto(userInfo);
        
        return ResponseEntity.ok(userDto);
    }
}
