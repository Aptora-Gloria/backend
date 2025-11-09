package com.oobeya.auth.application;

public interface JwtServicePort {
    String createToken(String login);
    String getLoginFromToken(String token);
}
