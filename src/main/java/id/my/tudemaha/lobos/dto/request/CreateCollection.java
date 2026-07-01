package id.my.tudemaha.lobos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCollection {

    @NotBlank(message = "name is required")
    @Size(min = 1, max = 100, message = "name must be between 1 and 100 characters")
    private String name;

    @NotBlank(message = "color is required")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "color must be a valid hex color (e.g. #FF5733)")
    private String color;
}
