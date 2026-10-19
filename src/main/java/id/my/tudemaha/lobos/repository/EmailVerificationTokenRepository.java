package id.my.tudemaha.lobos.repository;

import id.my.tudemaha.lobos.model.EmailVerificationToken;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class EmailVerificationTokenRepository {
    private final JdbcTemplate jdbcTemplate;

    public EmailVerificationTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<EmailVerificationToken> emailVerificationTokenRowMapper =
            (rs, rowNum) -> new EmailVerificationToken(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("token_hash"),
                    rs.getString("purpose"),
                    rs.getObject("used_at", LocalDateTime.class),
                    rs.getObject("expires_at", LocalDateTime.class),
                    rs.getObject("created_at", LocalDateTime.class));

    public void insert(EmailVerificationToken emailVerificationToken) {
        String sql = "INSERT INTO email_verification_tokens (user_id, token_hash, purpose, expires_at)"
                + " VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
                    ps.setString(1, emailVerificationToken.getUserId());
                    ps.setString(2, emailVerificationToken.getTokenHash());
                    ps.setString(3, emailVerificationToken.getPurpose());
                    ps.setObject(4, Timestamp.valueOf(emailVerificationToken.getExpiresAt()));

                    return ps;
                },
                keyHolder);
    }

    public Optional<EmailVerificationToken> findByTokenHash(String tokenHash) {
        String sql = "SELECT * FROM email_verification_tokens WHERE token_hash = ?";
        return jdbcTemplate.query(sql, emailVerificationTokenRowMapper, tokenHash).stream()
                .findFirst();
    }

    public void markAsUsed(String id) {
        String sql = "UPDATE email_verification_tokens SET used_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public int deleteExpiredTokens() {
        String sql = "DELETE FROM email_verification_tokens WHERE expires_at < (CURRENT_TIMESTAMP - INTERVAL 7 DAYS)";
        return jdbcTemplate.update(sql);
    }
}
