package id.my.tudemaha.lobos.security;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class JdbcPersistentTokenRepository implements PersistentTokenRepository {
    private static final String TOKEN_BY_SERIES_SQL = "SELECT username, series, token, last_used FROM persistent_logins WHERE series = ?";
    private static final String INSERT_TOKEN_SQL = "INSERT INTO persistent_logins (username, series, token, last_used) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_TOKEN_SQL = "UPDATE persistent_logins SET token = ?, last_used = ? WHERE series = ?";
    private static final String REMOVE_USER_TOKENS_SQL = "DELETE FROM persistent_logins WHERE username = ?";

    private final JdbcTemplate jdbcTemplate;

    public JdbcPersistentTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        jdbcTemplate.update(INSERT_TOKEN_SQL, token.getUsername(), token.getSeries(), token.getTokenValue(), token.getDate());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        jdbcTemplate.update(UPDATE_TOKEN_SQL, tokenValue, lastUsed, series);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        try {
            return jdbcTemplate.queryForObject(TOKEN_BY_SERIES_SQL, (rs, rowNum) -> new PersistentRememberMeToken(
                    rs.getString("username"),
                    rs.getString("series"),
                    rs.getString("token"),
                    rs.getTimestamp("last_used")
            ), seriesId);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public void removeUserTokens(String username) {
        jdbcTemplate.update(REMOVE_USER_TOKENS_SQL, username);
    }
}
