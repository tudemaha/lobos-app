package id.my.tudemaha.lobos.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class McpTokenData {
    String id;
    String name;
    String token;
    LocalDateTime createdAt;
    LocalDateTime lastUsedAt;
}
