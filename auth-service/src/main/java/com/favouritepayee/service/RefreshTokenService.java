package com.favouritepayee.service;

import com.favouritepayee.entity.RefreshToken;
import com.favouritepayee.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh-token.expiry-minutes:10080}")
    private long refreshTokenExpiryMinutes;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long customerId) {
        // Remove any existing refresh tokens for this customer
        refreshTokenRepository.deleteByCustomerId(customerId);

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString(),
                customerId,
                Instant.now().plus(refreshTokenExpiryMinutes, ChronoUnit.MINUTES)
        );
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isExpired());
    }

    @Transactional
    public void deleteByCustomerId(Long customerId) {
        refreshTokenRepository.deleteByCustomerId(customerId);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }
}
