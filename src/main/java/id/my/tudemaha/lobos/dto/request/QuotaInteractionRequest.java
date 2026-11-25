package id.my.tudemaha.lobos.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotaInteractionRequest {
    @NotBlank(message = "action is required")
    private String action;
}
