package id.my.tudemaha.lobos.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncGrammarData {
    private String id;
    private String collectionId;
    private String word;
    private String meaning;
    private String example;
    @JsonProperty("isStarred")
    private boolean isStarred;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
