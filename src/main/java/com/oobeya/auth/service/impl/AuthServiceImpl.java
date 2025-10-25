package com.oobeya.auth.service.impl;

import com.oobeya.auth.dto.AuthRequest;
import com.oobeya.auth.dto.AuthResponse;
import com.oobeya.auth.entity.OwnerType;
import com.oobeya.auth.dto.GitHubAccessTokenResponse;
import com.oobeya.auth.dto.GitHubUserDto;

import com.oobeya.auth.entity.User;
import com.oobeya.auth.mapper.GitHubUserMapper;
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
        // 1) GitHub access token al
        var tokenResponse = githubService.getAccessTokenFromCode(request.code());
        String accessToken = Optional.ofNullable(tokenResponse)
                .map(GitHubAccessTokenResponse::accessToken)
                .orElseThrow(() -> new RuntimeException("GitHub access token alınamadı"));

        // 2) Kullanıcı bilgisi çek
        GitHubUserDto apiUser = githubService.getUserInfo(accessToken);

        // 3) DTO'ya accessToken ekle (record immutable olduğu için yeni örnek)
        GitHubUserDto dto = new GitHubUserDto(
                apiUser.id(),
                apiUser.githubUserId(),
                apiUser.login(),
                accessToken,                 // <-- token burada
                apiUser.name(),
                apiUser.avatar_url(),
                apiUser.htmlUrl(),
                apiUser.ownerType(),
                apiUser.lastSyncedAt()
        );

        // 4) Upsert: önce githubUserId, yoksa login
        User entity = userRepository.findByGithubUserId(dto.githubUserId())
                .orElseGet(() -> userRepository.findByLogin(dto.login()).orElse(null));

        if (entity == null) {
            entity = GitHubUserMapper.toNewEntity(dto);
        } else {
            GitHubUserMapper.updateEntityFromDto(dto, entity);
            // Mapper set etse de garanti olsun:
            entity.setAccessToken(accessToken);
        }

        userRepository.save(entity);

        // 5) JWT üret ve dön
        String jwt = jwtService.createToken(entity.getLogin());
        return new AuthResponse(jwt);
    }



    @Override
    public GitHubUserDto getCurrentUser(String token) {
        String login = jwtService.getLoginFromToken(token);

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found for login: " + login));

        return GitHubUserMapper.toDto(user);
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
