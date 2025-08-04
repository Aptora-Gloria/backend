package com.oobeya.auth.service.impl;

import com.oobeya.auth.dto.AuthRequest;
import com.oobeya.auth.dto.AuthResponse;
import com.oobeya.github.dto.response.GitHubAccessTokenResponse;
import com.oobeya.github.dto.response.GitHubUserDto;

import com.oobeya.auth.entity.User;
import com.oobeya.auth.repository.UserRepository;
import com.oobeya.auth.service.AuthService;
import com.oobeya.github.exception.GitHubApiException;
import com.oobeya.auth.security.jwtservice.JwtService;
import com.oobeya.github.service.GitHubServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl  implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final GitHubServiceI githubService;


    @Override
    public AuthResponse login(AuthRequest request) {
        // 1. Access token al

        System.out.println("▶ GITHUB CODE: " + request.code());
        GitHubAccessTokenResponse tokenResponse = githubService.getAccessTokenFromCode(request.code());
        String accessToken = Optional.ofNullable(tokenResponse)
                .map(GitHubAccessTokenResponse::accessToken)
                .orElseThrow(() -> new RuntimeException("GitHub access token alınamadı"));

        System.out.println("▶ TOKEN RESPONSE: " + tokenResponse);

        // 2. GitHub kullanıcı bilgilerini al
        GitHubUserDto gitHubUser = githubService.getUserInfo(tokenResponse.accessToken());


        String login = Optional.ofNullable(gitHubUser)
                .map(GitHubUserDto::getLogin)
                .orElseThrow(() -> new RuntimeException("GitHub login bilgisi alınamadı"));


        User user = userRepository.findByLogin(gitHubUser.getLogin())
                .map(existingUser -> {
                    // Eğer kullanıcı varsa access token'ı güncelle
                    existingUser.setAccessToken(accessToken);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    // Eğer kullanıcı yoksa yeni kayıt oluştur
                    User newUser = User.builder()
                            .login(gitHubUser.getLogin())
                            .email(gitHubUser.getEmail())
                            .avatarUrl(gitHubUser.getAvatarUrl())
                            .accessToken(accessToken)
                            .build();
                    return userRepository.save(newUser);
                });


        // 5. JWT oluştur
        String jwt = jwtService.createToken(user.getLogin());

        // 6. Token'ı döndür
        return new AuthResponse(jwt);
    }


    @Override
    public GitHubUserDto getCurrentUser(String token) {
        String login = jwtService.getLoginFromToken(token);

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new GitHubUserDto(
                user.getLogin(),
                user.getEmail(),
                user.getLogin(),
                user.getAvatarUrl()
        );
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    public String getLoginFromToken(String token) {
        return jwtService.getLoginFromToken(token);
    }

    @Override
    public String getAccessTokenFromToken(String jwtToken) {
        String extractToken = extractToken(jwtToken);

        String login =getLoginFromToken(extractToken);
        User user = findByLogin(login)
                .orElseThrow(() -> new GitHubApiException("User not found: " + login));
        String accessToken = user.getAccessToken();
        return accessToken;
    }

    public static String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

}
