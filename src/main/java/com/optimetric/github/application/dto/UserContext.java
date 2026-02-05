package com.optimetric.github.application.dto;

public record UserContext(
        Long userId,
        String login,
        String accessToken
) {
}
