package id.my.tudemaha.lobos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGrammar {

    @NotBlank(message = "word is required")
    @Size(min = 1, max = 100, message = "word must be between 1 and 100 characters")
    private String word;

    @NotBlank(message = "meaning is required")
    private String meaning;

    private String example;
}
