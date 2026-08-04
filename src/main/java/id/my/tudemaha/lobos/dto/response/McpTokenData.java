package id.my.tudemaha.lobos.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class McpTokenData {
    String id;
    String name;
    String token;
    LocalDateTime createdAt;
    LocalDateTime lastUsedAt;
}
