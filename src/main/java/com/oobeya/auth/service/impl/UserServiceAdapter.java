package com.oobeya.auth.service.impl;


import com.oobeya.auth.entity.User;
import com.oobeya.auth.repository.UserRepository;
import com.oobeya.auth.service.UserServicePort;
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
