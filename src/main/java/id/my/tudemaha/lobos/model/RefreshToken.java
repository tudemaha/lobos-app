package id.my.tudemaha.lobos.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private String id;
    private String userId;
    private String familyId;
    private String tokenHash;
    private String deviceName;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;
}
