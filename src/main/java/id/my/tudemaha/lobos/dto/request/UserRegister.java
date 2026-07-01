package id.my.tudemaha.lobos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegister {

    @NotBlank(message = "firstName is required")
    @Size(min = 1, max = 100, message = "firstName must be between 1 and 100 characters")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @Size(min = 1, max = 100, message = "lastName must be between 1 and 100 characters")
    private String lastName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;

    @NotBlank(message = "confirmPassword is required")
    private String confirmPassword;
}
