package id.my.tudemaha.lobos.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String pendingEmail;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
