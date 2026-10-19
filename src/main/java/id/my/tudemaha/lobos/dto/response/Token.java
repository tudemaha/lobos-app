package id.my.tudemaha.lobos.dto.response;

import lombok.Data;

@Data
public class Token {
    private String accessToken;
    private String refreshToken;
}
