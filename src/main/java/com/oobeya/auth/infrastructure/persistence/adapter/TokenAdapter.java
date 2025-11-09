package com.oobeya.auth.infrastructure.persistence.adapter;

import com.oobeya.auth.application.TokenPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class TokenAdapter implements TokenPort {

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
    public String getLoginFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
