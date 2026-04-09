package com.usts.rag.web.security;

import com.usts.rag.common.security.AuthenticatedUser;
import com.usts.rag.web.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair createToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.getTtl());
        String sessionId = user.userId() + "-" + now.toEpochMilli();
        String token = Jwts.builder()
                .subject(user.userId())
                .claim("username", user.username())
                .claim("displayName", user.displayName())
                .claim("sessionId", sessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
        return new TokenPair(token, sessionId, expiresAt);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record TokenPair(String token, String sessionId, Instant expiresAt) {
    }
}
