package id.my.tudemaha.lobos.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class McpTokenList {
    public List<McpTokenData> mcpTokens;
    public PaginationResponse pagination;
}
