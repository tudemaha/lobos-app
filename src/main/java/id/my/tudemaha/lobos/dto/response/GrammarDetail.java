package id.my.tudemaha.lobos.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GrammarDetail {
    private String id;
    private String word;
    private String meaning;
    private String example;
    private boolean isStarred;
}
