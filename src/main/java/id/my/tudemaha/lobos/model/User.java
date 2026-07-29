package id.my.tudemaha.lobos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    @ToString.Exclude
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
