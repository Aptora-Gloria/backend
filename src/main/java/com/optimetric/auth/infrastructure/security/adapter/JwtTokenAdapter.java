package com.optimetric.auth.infrastructure.security.adapter;

import com.optimetric.auth.application.port.out.JwtTokenPort;
import com.optimetric.shared.exception.InvalidTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Infrastructure adapter for JWT token operations.
 * Implements JwtTokenPort using JWT library.
 */
@Component
public class JwtTokenAdapter implements JwtTokenPort {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    @Override
    public String createToken(String login) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        Key hmacKey = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName()
        );

        return Jwts.builder()
                .setSubject(login)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(hmacKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractLoginFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired token: " + e.getMessage(), e);
        }
    }
}
