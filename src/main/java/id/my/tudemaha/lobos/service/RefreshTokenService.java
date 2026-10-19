package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.exception.InvalidTokenException;
import id.my.tudemaha.lobos.model.RefreshToken;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.repository.RefreshTokenRepository;
import id.my.tudemaha.lobos.repository.UserRepository;
import id.my.tudemaha.lobos.utils.Hasher;
import id.my.tudemaha.lobos.utils.TokenUtil;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-ttl:30d}")
    private Duration refreshTtl;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public record TokenPair(String refreshToken, User user) {}

    public String generate(String userId, String deviceName) {
        String familyId = UUID.randomUUID().toString();
        return generateAndSaveToken(userId, familyId, deviceName);
    }

    @Transactional
    public TokenPair rotateRefreshToken(String rawOldToken) {
        String tokenHash = Hasher.hashToken(rawOldToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("invalid refresh token"));

        if (refreshToken.getRevokedAt() != null) {
            throw new InvalidTokenException("refresh token has been revoked");
        }

        if (refreshToken.getUsedAt() != null) {
            refreshTokenRepository.revokeFamily(refreshToken.getFamilyId());
            throw new InvalidTokenException("refresh token has been used");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("refresh token has expired");
        }

        refreshTokenRepository.markAsUsed(refreshToken.getId());
        User user = userRepository
                .findById(refreshToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("invalid user"));

        String newRawToken = generateAndSaveToken(
                refreshToken.getUserId(), refreshToken.getFamilyId(), refreshToken.getDeviceName());

        return new TokenPair(newRawToken, user);
    }

    public void revoke(String rawRefreshToken) {
        String tokenHash = Hasher.hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            refreshTokenRepository.revokeFamily(token.getFamilyId());
        });
    }

    public void revokeAll(User user) {
        revokeAll(user.getId());
    }

    public void revokeAll(String userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired refresh tokens");
        int deleteCount = refreshTokenRepository.deleteExpiredTokens();
        log.info("Finished cleanup: deleted {} expired refresh tokens", deleteCount);
    }

    private String generateAndSaveToken(String userId, String familyId, String deviceName) {
        String rawToken = TokenUtil.generate(32);
        String tokenHash = Hasher.hashToken(rawToken);

        LocalDateTime expiresAt = LocalDateTime.now().plus(refreshTtl);
        RefreshToken refreshToken =
                new RefreshToken(null, userId, familyId, tokenHash, deviceName, expiresAt, null, null, null);
        refreshTokenRepository.insert(refreshToken);
        return rawToken;
    }
}
