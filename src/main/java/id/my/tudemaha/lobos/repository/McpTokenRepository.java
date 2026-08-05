package id.my.tudemaha.lobos.repository;

import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.model.McpToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class McpTokenRepository {
    private final JdbcTemplate jdbcTemplate;

    public McpTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<McpToken> mcpTokenRowMapper = (rs, rowNum) -> new McpToken(
            rs.getString("id"),
            rs.getString("user_id"),
            rs.getString("name"),
            rs.getString("token"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getObject("last_used_at", LocalDateTime.class)
    );

    public void insert(McpToken mcpToken) {
        String sql = "INSERT INTO mcp_tokens (user_id, name, token)" +
                " VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, mcpToken.getUserId());
            ps.setString(2, mcpToken.getName());
            ps.setString(3, mcpToken.getToken());

            return ps;
        }, keyHolder);
    }

    public record PaginatedMcpToken(List<McpToken> mcpTokens, int totalCount) {}

    public PaginatedMcpToken findAllByUserId(String userId, PaginationRequest paginationRequest) {
        int limit = paginationRequest.getPerPage();
        int offset = (paginationRequest.getPage() - 1) * limit;

        List<Object> params = new ArrayList<>();

        String baseQuery = " FROM mcp_tokens WHERE user_id = ?";
        params.add(userId);

        if (paginationRequest.getQuery() != null && !paginationRequest.getQuery().isBlank()) {
            baseQuery += " AND name LIKE ?";
            params.add("%" + paginationRequest.getQuery() + "%");
        }

        String countQuery = "SELECT COUNT(*)" + baseQuery;
        Integer count = jdbcTemplate.queryForObject(countQuery, Integer.class, params.toArray());
        int totalCount = count != null ? count : 0;

        String query = "SELECT *" + baseQuery + " LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);

        List<McpToken> mcpTokens = jdbcTemplate.query(query, mcpTokenRowMapper, params.toArray());
        return new PaginatedMcpToken(mcpTokens, totalCount);
    }

    public Optional<McpToken> getMcpTokenById(String id) {
        String sql = "SELECT * FROM mcp_tokens WHERE id = ?";
        return jdbcTemplate.query(sql, mcpTokenRowMapper, id).stream().findFirst();
    }

    public Optional<McpToken> getMcpTokenByToken(String token) {
        String sql = "SELECT * FROM mcp_tokens WHERE token = ?";
        return jdbcTemplate.query(sql, mcpTokenRowMapper, token).stream().findFirst();
    }

    public void updateName(McpToken mcpToken) {
        String sql = "UPDATE mcp_tokens SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, mcpToken.getName(), mcpToken.getId());
    }

    public void updateLastUsed(McpToken mcpToken) {
        String sql = "UPDATE mcp_tokens SET last_used_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, LocalDateTime.now(), mcpToken.getId());
    }

    public void delete(String id) {
        String sql = "DELETE FROM mcp_tokens WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
