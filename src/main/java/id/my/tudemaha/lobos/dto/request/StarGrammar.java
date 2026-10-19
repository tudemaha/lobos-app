package id.my.tudemaha.lobos.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StarGrammar {
    @JsonProperty("isStarred")
    private boolean isStarred;
}
