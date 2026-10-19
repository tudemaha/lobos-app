package id.my.tudemaha.lobos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPassword {
    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    @Size(min = 8, message = "Password confirmation must be at least 8 characters")
    private String confirmPassword;
}
