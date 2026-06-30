package id.my.tudemaha.lobos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
