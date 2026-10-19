package id.my.tudemaha.lobos.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grammar {
    private String id;
    private String word;
    private String meaning;
    private String example;
    private boolean isStarred;
    private String collectionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
