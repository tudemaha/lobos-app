package id.my.tudemaha.lobos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {
    private String id;
    private String userId;
    private String tokenHash;
    private String purpose;
    private LocalDateTime usedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
