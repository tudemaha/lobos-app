package id.my.tudemaha.lobos.repository;

import id.my.tudemaha.lobos.model.Collection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class CollectionRepository {
    private final JdbcTemplate jdbcTemplate;

    public CollectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Collection> collectionRowMapper = (rs, rowNum) -> new Collection(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("color"),
            rs.getString("user_id"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getObject("updated_at", LocalDateTime.class)
    );

    public void insert(Collection collection) {
        String sql = "INSERT INTO collections (name, color, user_id)" +
                " VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, collection.getName());
            ps.setString(2, collection.getColor());
            ps.setString(3, collection.getUserId());

            return ps;
        }, keyHolder);
    }

    public List<Collection> findAllByUserId(String userId) {
        String sql = "SELECT * FROM collections WHERE user_id = ?";
        return jdbcTemplate.query(sql, collectionRowMapper, userId);
    }

    public Optional<Collection> findById(String id) {
        String sql = "SELECT * FROM collections WHERE id = ?";
        return jdbcTemplate.query(sql, collectionRowMapper, id).stream().findFirst();
    }

    public void update(Collection collection) {
        String sql = "UPDATE collections SET name = ?, color = ? WHERE id = ?";
        jdbcTemplate.update(sql, collection.getName(), collection.getColor(), collection.getId());
    }

    public void delete(String id) {
        String sql = "DELETE FROM collections WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
