package com.optimetric.auth.application;

import com.optimetric.auth.infrastructure.persistence.entity.User;

import java.util.Optional;

public interface UserServicePort {
    Optional<User> findByLogin(String login);
}
