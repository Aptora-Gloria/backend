package com.oobeya.auth.application;

import com.oobeya.auth.infrastructure.persistence.entity.User;

import java.util.Optional;

public interface UserServicePort {
    Optional<User> findByLogin(String login);
}
