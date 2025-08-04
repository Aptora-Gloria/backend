package com.oobeya.auth.service;

public interface JwtServicePort {
    String createToken(String login);
    String getLoginFromToken(String token);
}
