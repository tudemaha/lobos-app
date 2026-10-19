package id.my.tudemaha.lobos.repository;

import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.model.Collection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

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
            rs.getObject("updated_at", LocalDateTime.class),
            rs.getObject("deleted_at", LocalDateTime.class));

    public void insert(Collection collection) {
        String sql = "INSERT INTO collections (id, name, color, user_id)" + " VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
                    ps.setString(1, collection.getId());
                    ps.setString(2, collection.getName());
                    ps.setString(3, collection.getColor());
                    ps.setString(4, collection.getUserId());

                    return ps;
                },
                keyHolder);
    }

    public record PaginatedCollection(List<Collection> collections, int totalCount) {}

    public PaginatedCollection findAllByUserId(String userId, PaginationRequest paginationRequest) {
        int limit = paginationRequest.getPerPage();
        int offset = (paginationRequest.getPage() - 1) * limit;

        List<Object> params = new ArrayList<>();

        String baseQuery = " FROM collections WHERE user_id = ? AND deleted_at IS NULL";
        params.add(userId);

        if (paginationRequest.getQuery() != null
                && !paginationRequest.getQuery().isBlank()) {
            baseQuery += " AND name LIKE ?";
            params.add("%" + paginationRequest.getQuery() + "%");
        }

        String countQuery = "SELECT COUNT(*)" + baseQuery;
        Integer count = jdbcTemplate.queryForObject(countQuery, Integer.class, params.toArray());
        int totalCount = count != null ? count : 0;

        String query = "SELECT *" + baseQuery + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);

        List<Collection> collections = jdbcTemplate.query(query, collectionRowMapper, params.toArray());
        return new PaginatedCollection(collections, totalCount);
    }

    public Optional<Collection> findById(String id) {
        String sql = "SELECT * FROM collections WHERE id = ? AND deleted_at IS NULL";
        return jdbcTemplate.query(sql, collectionRowMapper, id).stream().findFirst();
    }

    public void update(Collection collection) {
        String sql = "UPDATE collections SET name = ?, color = ? WHERE id = ? AND deleted_at IS NULL";
        jdbcTemplate.update(sql, collection.getName(), collection.getColor(), collection.getId());
    }

    public void delete(String id) {
        String sql = "UPDATE collections SET deleted_at = CURRENT_TIMESTAMP(3) WHERE id = ? AND deleted_at IS NULL";
        jdbcTemplate.update(sql, id);
    }

    public List<Collection> findSyncChanges(String userId, LocalDateTime since, String lastId, int limit) {
        if (since == null || lastId == null) {
            String sql = "SELECT * FROM collections WHERE user_id = ? " + "ORDER BY updated_at, id LIMIT ?";
            return jdbcTemplate.query(sql, collectionRowMapper, userId, limit);
        }

        String sql = "SELECT * FROM collections WHERE user_id = ? "
                + "AND (updated_at > ? OR (updated_at = ? AND id > ?)) " + "ORDER BY updated_at, id LIMIT ?";
        return jdbcTemplate.query(sql, collectionRowMapper, userId, since, since, lastId, limit);
    }

    public int purgeOldTombstones(LocalDateTime threshold) {
        String sql = "DELETE FROM collections WHERE deleted_at IS NOT NULL and deleted_at < ?";
        return jdbcTemplate.update(sql, threshold);
    }
}
