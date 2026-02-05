package com.optimetric.auth.infrastructure.persistence.adapter;

import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.auth.application.port.out.UserPersistencePort;
import com.optimetric.auth.infrastructure.core.mapper.UserMapper;
import com.optimetric.auth.infrastructure.persistence.entity.User;
import com.optimetric.auth.infrastructure.persistence.jpa.repository.UserRepository;
import com.optimetric.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Kullanıcı kalıcılığı için altyapı adapter'ı; UserPersistencePort, JPA ile uygular.
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserRepository userRepository;

    @Override
    public UserInfo save(UserInfo userInfo) {
        // Mevcut kullanıcıyı id, github user id veya login ile ara
        User entity = null;
        if (userInfo.id() != null) {
            entity = userRepository.findById(userInfo.id()).orElse(null);
        }
        
        if (entity == null && userInfo.githubUserId() != null) {
            entity = userRepository.findByGithubUserId(userInfo.githubUserId()).orElse(null);
        }
        
        if (entity == null && userInfo.login() != null) {
            entity = userRepository.findByLogin(userInfo.login()).orElse(null);
        }

        // Entity yoksa yeni oluştur, varsa güncelle
        if (entity == null) {
            entity = UserMapper.toNewEntity(userInfo);
        } else {
            UserMapper.updateEntityFromUserInfo(userInfo, entity);
        }

        // Kaydet ve DTO'ya çevir
        User savedEntity = userRepository.save(entity);
        return UserMapper.toUserInfo(savedEntity);
    }

    @Override
    public Optional<UserInfo> findByLogin(String login) {
        return userRepository.findByLogin(login)
                .map(UserMapper::toUserInfo);
    }

    @Override
    public Optional<UserInfo> findByGithubUserId(Long githubUserId) {
        return userRepository.findByGithubUserId(githubUserId)
                .map(UserMapper::toUserInfo);
    }

    @Override
    public Optional<UserInfo> findById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toUserInfo);
    }
}
