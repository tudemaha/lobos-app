package id.my.tudemaha.lobos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGrammar {
    @Pattern(
            regexp = "^$|^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "id must be a valid UUID"
    )
    private String id;

    @NotBlank(message = "word is required")
    @Size(min = 1, max = 100, message = "word must be between 1 and 100 characters")
    private String word;

    @NotBlank(message = "meaning is required")
    private String meaning;

    private String example;
}
