package com.optimetric.auth.infrastructure.core.mapper;

import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.shared.domain.OwnerType;

import java.time.OffsetDateTime;
import java.util.Locale;

public final class UserMapper {

    private UserMapper() {}

    // 🔹 UserInfo (Application DTO) -> NEW ENTITY
    public static User toNewEntity(UserInfo userInfo) {
        if (userInfo == null) return null;

        var now = OffsetDateTime.now();
        return User.builder()
                .githubUserId(userInfo.githubUserId())
                .login(userInfo.login())
                .accessToken(userInfo.accessToken())
                .loginCi(userInfo.login() != null ? userInfo.login().toLowerCase(Locale.ROOT) : null)
                .name(userInfo.name())
                .avatar_url(userInfo.avatarUrl())
                .htmlUrl(userInfo.htmlUrl())
                .ownerType(userInfo.ownerType())
                .createdAt(now)
                .updatedAt(now)
                .lastSyncedAt(now)
                .build();
    }

    // 🔹 UserInfo (Application DTO) -> UPDATE ENTITY
    public static void updateEntityFromUserInfo(UserInfo userInfo, User entity) {
        if (userInfo == null || entity == null) return;

        if (userInfo.githubUserId() != null)
            entity.setGithubUserId(userInfo.githubUserId());

        if (userInfo.login() != null) {
            entity.setLogin(userInfo.login());
            entity.setLoginCi(userInfo.login().toLowerCase(Locale.ROOT));
        }

        if (userInfo.accessToken() != null) entity.setAccessToken(userInfo.accessToken());
        if (userInfo.name() != null)      entity.setName(userInfo.name());
        if (userInfo.avatarUrl() != null) entity.setAvatar_url(userInfo.avatarUrl());
        if (userInfo.htmlUrl() != null)   entity.setHtmlUrl(userInfo.htmlUrl());

        entity.setOwnerType(userInfo.ownerType());
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setLastSyncedAt(OffsetDateTime.now());
    }

    // 🔹 ENTITY -> UserInfo (Application DTO)
    public static UserInfo toUserInfo(User entity) {
        if (entity == null) return null;

        return new UserInfo(
                entity.getId(),
                entity.getGithubUserId(),
                entity.getLogin(),
                entity.getAccessToken(),
                entity.getName(),
                entity.getAvatar_url(),
                entity.getHtmlUrl(),
                entity.getOwnerType(),
                entity.getLastSyncedAt()
        );
    }
}
