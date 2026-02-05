package com.optimetric.github.application.port.out;

import java.util.Optional;

import com.optimetric.github.application.dto.UserContext;

public interface UserContextPort {
    Optional<UserContext> getContextByLogin(String login);
}
