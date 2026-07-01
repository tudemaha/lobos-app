package id.my.tudemaha.lobos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePassword {

    @NotBlank(message = "oldPassword is required")
    private String oldPassword;

    @NotBlank(message = "newPassword is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    private String newPassword;

    @NotBlank(message = "confirmNewPassword is required")
    private String confirmNewPassword;
}
