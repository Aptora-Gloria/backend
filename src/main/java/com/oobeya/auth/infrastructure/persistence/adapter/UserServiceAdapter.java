package com.oobeya.auth.infrastructure.persistence.adapter;


import com.oobeya.auth.infrastructure.persistence.entity.User;
import com.oobeya.auth.infrastructure.persistence.jpa.repository.UserRepository;
import com.oobeya.auth.application.UserServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceAdapter implements UserServicePort {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}
