package id.my.tudemaha.lobos.dto.request;

import lombok.Data;

@Data
public class UserRegister {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
