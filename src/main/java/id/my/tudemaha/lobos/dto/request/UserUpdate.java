package id.my.tudemaha.lobos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdate {

    @NotBlank(message = "firstName is required")
    @Size(min = 1, max = 100, message = "firstName must be between 1 and 100 characters")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @Size(min = 1, max = 100, message = "lastName must be between 1 and 100 characters")
    private String lastName;
}
