package com.optimetric.auth.infrastructure.persistence.adapter;

import com.optimetric.auth.application.TokenPort;
import com.optimetric.auth.application.JwtServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceAdapter implements JwtServicePort {

    private final TokenPort tokenPort;

    @Override
    public String createToken(String login) {
        return tokenPort.createToken(login);
    }

    @Override
    public String getLoginFromToken(String token) {
        return tokenPort.getLoginFromToken(token);
    }
}
