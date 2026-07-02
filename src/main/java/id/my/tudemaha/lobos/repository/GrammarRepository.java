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
            rs.getObject("updated_at", LocalDateTime.class)
    );

    public void insert(Grammar grammar) {
        String sql = "INSERT INTO grammars (word, meaning, example, collection_id)"+
                " VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, grammar.getWord());
            ps.setString(2, grammar.getMeaning());
            ps.setString(4, grammar.getCollectionId());

            if(grammar.getExample() != null) {
                ps.setString(3, grammar.getExample());
            } else{
                ps.setNull(3, Types.VARCHAR);
            }

            return ps;
        }, keyHolder);
    }

    public record PaginatedGrammar(List<Grammar> grammars, int totalCount) {}

    public PaginatedGrammar findAllByCollectionId(String collectionId, PaginationRequest paginationRequest) {
        int limit = paginationRequest.getPerPage();
        int offset = (paginationRequest.getPage() - 1) * limit;

        List<Object> params = new ArrayList<>();

        String baseQuery = " FROM grammars WHERE collection_id = ?";
        params.add(collectionId);

        if (paginationRequest.getQuery() != null && !paginationRequest.getQuery().isBlank()) {
            baseQuery += " AND word LIKE ?";
            params.add("%" + paginationRequest.getQuery() + "%");
        }

        String queryCount = "SELECT COUNT(*)" + baseQuery;
        Integer count = jdbcTemplate.queryForObject(queryCount, Integer.class, params.toArray());
        int totalCount = count != null ? count : 0;

        String query = "SELECT *" + baseQuery + " ORDER BY word LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);

        List<Grammar> grammars = jdbcTemplate.query(query, grammarRowMapper, params.toArray());
        return new PaginatedGrammar(grammars, totalCount);
    }

    public Optional<Grammar> findById(String id) {
        String sql = "SELECT * FROM grammars WHERE id = ?";
        return jdbcTemplate.query(sql, grammarRowMapper, id).stream().findFirst();
    }

    public void toggleStarred(String collectionId) {
        Optional<Grammar> grammarOpt = findById(collectionId);
        if(grammarOpt.isEmpty()) {
            throw new NotFoundException();
        }

        Grammar grammar = grammarOpt.get();
        String sql = "UPDATE grammars SET is_starred = ? WHERE id = ?";
        jdbcTemplate.update(sql, !grammar.isStarred(), grammar.getId());
    }

    public void update(Grammar grammar) {
        String sql = "UPDATE grammars SET word = ?, meaning = ?, example = ? WHERE id = ?";
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
        String sql = "DELETE FROM grammars WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
