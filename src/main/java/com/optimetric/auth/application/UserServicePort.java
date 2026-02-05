package com.optimetric.auth.application;

import com.optimetric.auth.application.dto.UserInfo;

import java.util.Optional;

public interface UserServicePort {
    Optional<UserInfo> findByLogin(String login);
}
