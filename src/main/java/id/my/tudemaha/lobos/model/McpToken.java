package id.my.tudemaha.lobos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpToken {
    private String id;
    private String userId;
    private String name;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
}
