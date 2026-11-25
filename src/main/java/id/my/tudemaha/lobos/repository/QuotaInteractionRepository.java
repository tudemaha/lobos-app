package id.my.tudemaha.lobos.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class QuotaInteractionRepository {
    private final JdbcTemplate jdbcTemplate;

    public QuotaInteractionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(String id, String userId, String action) {
        String sql = "INSERT INTO collection_quota_interactions (id, user_id, action) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, id, userId, action);
    }

    public boolean hasUserNotifiedDeveloper(String userId) {
        String sql =
                "SELECT COUNT(*) FROM collection_quota_interactions WHERE user_id = ? AND action = 'NOTIFY_DEVELOPER'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }
}
