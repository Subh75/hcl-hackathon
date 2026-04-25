package com.favouritepayee.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiry-minutes}")
    private long jwtExpiryMinutes;

    public String generateToken(Long customerId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(customerId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtExpiryMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey())
                .compact();
    }

    public Optional<Long> extractCustomerId(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.of(Long.valueOf(subject));
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
