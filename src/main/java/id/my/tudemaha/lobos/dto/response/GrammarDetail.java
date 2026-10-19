package id.my.tudemaha.lobos.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GrammarDetail {
    private String id;
    private String word;
    private String meaning;
    private String example;

    @JsonProperty("isStarred")
    private boolean isStarred;
}
