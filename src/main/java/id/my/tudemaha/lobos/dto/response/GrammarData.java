package id.my.tudemaha.lobos.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GrammarData {
    private String id;
    private String word;
    private String meaning;
    private String example;

    @JsonProperty("isStarred")
    private boolean isStarred;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
