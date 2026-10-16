package id.my.tudemaha.lobos.repository;

import id.my.tudemaha.lobos.model.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class RefreshTokenRepository {
    private final JdbcTemplate jdbcTemplate;

    public RefreshTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RefreshToken> refreshTokenRowMapper = (rs, rowNum) -> new RefreshToken(
            rs.getString("id"),
            rs.getString("user_id"),
            rs.getString("family_id"),
            rs.getString("token_hash"),
            rs.getString("device_name"),
            rs.getObject("expires_at", LocalDateTime.class),
            rs.getObject("used_at", LocalDateTime.class),
            rs.getObject("revoked_at", LocalDateTime.class),
            rs.getObject("created_at", LocalDateTime.class)
    );

    public void insert(RefreshToken refreshToken) {
        String sql = "INSERT INTO refresh_tokens (user_id, family_id, token_hash, device_name, expires_at)" +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, refreshToken.getUserId());
            ps.setString(2, refreshToken.getFamilyId());
            ps.setString(3, refreshToken.getTokenHash());
            ps.setString(4, refreshToken.getDeviceName());
            ps.setObject(5, Timestamp.valueOf(refreshToken.getExpiresAt()));

            return ps;
        }, keyHolder);
    }

    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        String sql = "SELECT * FROM refresh_tokens WHERE token_hash = ?";
        return jdbcTemplate.query(sql, refreshTokenRowMapper, tokenHash).stream().findFirst();
    }

    public void markAsUsed(String id) {
        String sql = "UPDATE refresh_tokens SET used_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void revokeFamily(String familyId) {
        String sql = "UPDATE refresh_tokens SET revoked_at = CURRENT_TIMESTAMP " +
                "WHERE family_id = ? AND revoked_at IS NULL";
        jdbcTemplate.update(sql, familyId);
    }

    public void revokeAllByUserId(String userId) {
        String sql = "UPDATE refresh_tokens SET revoked_at = CURRENT_TIMESTAMP " +
                "WHERE user_id = ? AND revoked_at IS NULL";
        jdbcTemplate.update(sql, userId);
    }

    public int deleteExpiredTokens() {
        String sql = "DELETE FROM refresh_tokens WHERE expires_at < (CURRENT_TIMESTAMP - INTERVAL 7 DAY)";
        return jdbcTemplate.update(sql);
    }
}
