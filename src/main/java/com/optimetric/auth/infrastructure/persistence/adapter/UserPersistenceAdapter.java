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

/**
 * Infrastructure adapter for user persistence operations.
 * Implements UserPersistencePort using JPA UserRepository.
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserRepository userRepository;

    @Override
    public UserInfo save(UserInfo userInfo) {
        // Find existing user by ID, GitHub user ID, or login
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

        // Create or update entity
        if (entity == null) {
            entity = UserMapper.toNewEntity(userInfo);
        } else {
            UserMapper.updateEntityFromUserInfo(userInfo, entity);
        }

        // Save and convert back to DTO
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
}
