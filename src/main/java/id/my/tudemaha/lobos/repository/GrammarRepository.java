package id.my.tudemaha.lobos.repository;

import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.exception.NotFoundException;
import id.my.tudemaha.lobos.model.Grammar;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class GrammarRepository {
    private final JdbcTemplate jdbcTemplate;

    public GrammarRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Grammar> grammarRowMapper = (rs, rowNum) -> new Grammar(
            rs.getString("id"),
            rs.getString("word"),
            rs.getString("meaning"),
            rs.getString("example"),
            rs.getBoolean("is_starred"),
            rs.getString("collection_id"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getObject("updated_at", LocalDateTime.class),
            rs.getObject("deleted_at", LocalDateTime.class)
    );

    public Grammar insert(Grammar grammar) {
        String sql = "INSERT INTO grammars (id, word, meaning, example, collection_id)"+
                " VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, grammar.getId());
            ps.setString(2, grammar.getWord());
            ps.setString(3, grammar.getMeaning());
            ps.setString(4, grammar.getExample());
            ps.setString(5, grammar.getCollectionId());

            return ps;
        }, keyHolder);

        return grammar;
    }

    public record PaginatedGrammar(List<Grammar> grammars, int totalCount) {}

    public PaginatedGrammar findAllByCollectionId(String collectionId, PaginationRequest paginationRequest) {
        int limit = paginationRequest.getPerPage();
        int offset = (paginationRequest.getPage() - 1) * limit;

        List<Object> params = new ArrayList<>();

        String baseQuery = " FROM grammars WHERE collection_id = ? AND deleted_at IS NULL";
        params.add(collectionId);

        if (paginationRequest.getQuery() != null && !paginationRequest.getQuery().isBlank()) {
            baseQuery += " AND (word LIKE ? OR meaning LIKE ?)";
            params.add("%" + paginationRequest.getQuery() + "%");
            params.add("%" + paginationRequest.getQuery() + "%");
        }

        String queryCount = "SELECT COUNT(*)" + baseQuery;
        Integer count = jdbcTemplate.queryForObject(queryCount, Integer.class, params.toArray());
        int totalCount = count != null ? count : 0;

        String query = "SELECT *" + baseQuery + " ORDER BY is_starred DESC, word LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);

        List<Grammar> grammars = jdbcTemplate.query(query, grammarRowMapper, params.toArray());
        return new PaginatedGrammar(grammars, totalCount);
    }

    public Optional<Grammar> findById(String id) {
        String sql = "SELECT * FROM grammars WHERE id = ? AND deleted_at IS NULL";
        return jdbcTemplate.query(sql, grammarRowMapper, id).stream().findFirst();
    }

    public void toggleStarred(String collectionId) {
        Optional<Grammar> grammarOpt = findById(collectionId);
        if(grammarOpt.isEmpty()) {
            throw new NotFoundException();
        }

        Grammar grammar = grammarOpt.get();
        String sql = "UPDATE grammars SET is_starred = ? WHERE id = ? AND deleted_at IS NULL";
        jdbcTemplate.update(sql, !grammar.isStarred(), grammar.getId());
    }

    public void update(Grammar grammar) {
        String sql = "UPDATE grammars SET word = ?, meaning = ?, example = ? WHERE id = ? AND deleted_at IS NULL";
        Object[] params = new Object[] {
            grammar.getWord(),
            grammar.getMeaning(),
            grammar.getExample(),
            grammar.getId()
        };
        int[] types = new int[] {
            Types.VARCHAR,
            Types.VARCHAR,
            Types.VARCHAR,
            Types.VARCHAR
        };
        jdbcTemplate.update(sql, params, types);
    }

    public void delete(String id) {
        String sql = "UPDATE grammars SET deleted_at = CURRENT_TIMESTAMP(3) WHERE id = ? AND deleted_at IS NULL";
        jdbcTemplate.update(sql, id);
    }

    public void deleteAllByCollectionId(String collectionId) {
        String sql = "UPDATE grammars SET deleted_at = CURRENT_TIMESTAMP(3) WHERE collection_id = ? AND deleted_at IS NULL";
        jdbcTemplate.update(sql, collectionId);
    }
}
