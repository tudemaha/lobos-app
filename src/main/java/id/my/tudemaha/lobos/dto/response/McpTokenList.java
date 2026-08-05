package id.my.tudemaha.lobos.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class McpTokenList {
    public List<McpTokenData> mcpTokens;
    public PaginationResponse pagination;
}
