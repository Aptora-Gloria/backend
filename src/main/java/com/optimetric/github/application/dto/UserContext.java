package com.optimetric.github.application.dto;

/**
 * User context DTO containing minimal user information needed by application layer.
 * This avoids exposing infrastructure entities (User) to the application layer.
 */
public record UserContext(
        Long userId,
        String login,
        String accessToken
) {
}
