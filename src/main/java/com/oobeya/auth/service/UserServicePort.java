package com.oobeya.auth.service;

import com.oobeya.auth.entity.User;

import java.util.Optional;

public interface UserServicePort {
    Optional<User> findByLogin(String login);
}
