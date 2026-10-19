package id.my.tudemaha.lobos.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GrammarData {
    private String id;
    private String word;
    private String meaning;
    private boolean isStarred;
    private LocalDateTime createdAt;
}
