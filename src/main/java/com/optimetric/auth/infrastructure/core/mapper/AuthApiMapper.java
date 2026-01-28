package com.optimetric.auth.infrastructure.core.mapper;

import com.optimetric.auth.api.dto.AuthRequest;
import com.optimetric.auth.api.dto.AuthResponse;
import com.optimetric.auth.api.dto.GitHubUserDto;
import com.optimetric.auth.application.dto.LoginCommand;
import com.optimetric.auth.application.dto.LoginResult;
import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.shared.domain.OwnerType;

import java.time.OffsetDateTime;
import java.util.Locale;

public final class AuthApiMapper {

    private AuthApiMapper() {}

    public static LoginCommand toLoginCommand(AuthRequest request) {
        if (request == null) return null;
        return new LoginCommand(request.code());
    }

    public static AuthResponse toAuthResponse(LoginResult result) {
        if (result == null) return null;
        return new AuthResponse(result.jwtToken());
    }

    public static UserInfo toUserInfo(GitHubUserDto dto) {
        if (dto == null) return null;
        return new UserInfo(
                dto.id(),
                dto.githubUserId(),
                dto.login(),
                dto.accessToken(),
                dto.name(),
                dto.avatar_url(),
                dto.htmlUrl(),
                mapOwnerType(dto.ownerType()),
                dto.lastSyncedAt() != null ? OffsetDateTime.parse(dto.lastSyncedAt()) : null
        );
    }

    public static GitHubUserDto toGitHubUserDto(UserInfo userInfo) {
        if (userInfo == null) return null;
        return new GitHubUserDto(
                userInfo.id(),
                userInfo.githubUserId(),
                userInfo.login(),
                userInfo.accessToken(),
                userInfo.name(),
                userInfo.avatarUrl(),
                userInfo.htmlUrl(),
                userInfo.ownerType() != null ? userInfo.ownerType().name() : null,
                userInfo.lastSyncedAt() != null ? userInfo.lastSyncedAt().toString() : null
        );
    }

    private static OwnerType mapOwnerType(String s) {
        if (s == null) return OwnerType.USER;
        return s.equalsIgnoreCase("Organization") ? OwnerType.ORGANIZATION : OwnerType.USER;
    }
}
