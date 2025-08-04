package com.oobeya.auth.service.impl;

import com.oobeya.auth.security.jwtservice.JwtService;
import com.oobeya.auth.service.JwtServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceAdapter implements JwtServicePort {

    private final JwtService jwtService;

    @Override
    public String createToken(String login) {
        return jwtService.createToken(login);
    }

    @Override
    public String getLoginFromToken(String token) {
        return jwtService.getLoginFromToken(token);
    }
}
