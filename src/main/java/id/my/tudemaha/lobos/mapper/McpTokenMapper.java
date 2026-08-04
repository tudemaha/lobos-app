package id.my.tudemaha.lobos.mapper;

import id.my.tudemaha.lobos.dto.request.CreateMcpToken;
import id.my.tudemaha.lobos.dto.response.McpTokenData;
import id.my.tudemaha.lobos.model.McpToken;

public class McpTokenMapper {
    public static McpToken toEntity(CreateMcpToken createMcpToken) {
        McpToken mcpToken = new McpToken();
        mcpToken.setName(createMcpToken.getName());

        return mcpToken;
    }

    public static McpTokenData toDto(McpToken mcpToken) {
        McpTokenData mcpTokenData = new McpTokenData();
        mcpTokenData.setId(mcpToken.getId());
        mcpTokenData.setName(mcpToken.getName());
        mcpTokenData.setCreatedAt(mcpToken.getCreatedAt());
        mcpTokenData.setLastUsedAt(mcpToken.getLastUsedAt());

        String token = mcpToken.getToken();
        int tokenLength = token.length();
        mcpTokenData.setToken(token.substring(0, 5)
                        + "******"
                        + token.substring(tokenLength - 5)
        );

        return mcpTokenData;
    }
}
