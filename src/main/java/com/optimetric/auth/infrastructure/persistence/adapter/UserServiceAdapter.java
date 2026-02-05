package com.optimetric.auth.infrastructure.persistence.adapter;

import java.util.Optional;

import com.optimetric.auth.application.UserServicePort;
import com.optimetric.auth.application.dto.UserInfo;
import com.optimetric.auth.infrastructure.core.mapper.UserMapper;
import com.optimetric.auth.infrastructure.persistence.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceAdapter implements UserServicePort {

    private final UserRepository userRepository;

    @Override
    public Optional<UserInfo> findByLogin(String login) {
        return userRepository.findByLogin(login).map(UserMapper::toUserInfo);
    }
}
