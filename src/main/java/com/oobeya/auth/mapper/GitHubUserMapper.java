package com.oobeya.auth.mapper;



import com.oobeya.auth.entity.User;
import com.oobeya.auth.entity.OwnerType;
import com.oobeya.auth.dto.GitHubUserDto;

import java.time.OffsetDateTime;
import java.util.Locale;

public final class GitHubUserMapper {

    private GitHubUserMapper() {}

    // 🔹 API veya DTO -> NEW ENTITY
    public static User toNewEntity(GitHubUserDto dto) {
        if (dto == null) return null;

        var now = OffsetDateTime.now();
        return User.builder()
                .githubUserId(dto.githubUserId())
                .login(dto.login())
                .accessToken(dto.accessToken())
                .loginCi(dto.login() != null ? dto.login().toLowerCase(Locale.ROOT) : null)
                .name(dto.name())
                .avatar_url(dto.avatar_url())
                .htmlUrl(dto.htmlUrl())
                .ownerType(mapOwnerType(dto.ownerType()))
                .createdAt(now)
                .updatedAt(now)
                .lastSyncedAt(now)
                .build();
    }

    // 🔹 DTO -> UPDATE ENTITY
    public static void updateEntityFromDto(GitHubUserDto dto, User entity) {
        if (dto == null || entity == null) return;

        if (dto.githubUserId() != null)
            entity.setGithubUserId(dto.githubUserId());

        if (dto.login() != null) {
            entity.setLogin(dto.login());
            entity.setLoginCi(dto.login().toLowerCase(Locale.ROOT));
        }

        if (dto.name() != null)      entity.setName(dto.name());
        if (dto.avatar_url() != null) entity.setAvatar_url(dto.avatar_url());
        if (dto.htmlUrl() != null)   entity.setHtmlUrl(dto.htmlUrl());

        entity.setOwnerType(mapOwnerType(dto.ownerType()));
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setLastSyncedAt(OffsetDateTime.now());
    }

    // 🔹 ENTITY -> DTO (UI response)
    public static GitHubUserDto toDto(User entity) {
        if (entity == null) return null;

        return new GitHubUserDto(
                entity.getId(),
                entity.getGithubUserId(),
                entity.getLogin(),
                entity.getAccessToken(),
                entity.getName(),
                entity.getAvatar_url(),
                entity.getHtmlUrl(),
                entity.getOwnerType() != null ? entity.getOwnerType().name() : null,
                entity.getLastSyncedAt() != null ? entity.getLastSyncedAt().toString() : null
        );
    }

    private static OwnerType mapOwnerType(String s) {
        if (s == null) return OwnerType.USER;
        return s.equalsIgnoreCase("Organization") ? OwnerType.ORGANIZATION : OwnerType.USER;
    }
}

