package id.my.tudemaha.lobos.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GrammarData {
    private String id;
    private String word;
    private String meaning;
    private boolean isStarred;
    private LocalDateTime createdAt;
}
