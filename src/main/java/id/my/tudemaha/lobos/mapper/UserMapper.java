package id.my.tudemaha.lobos.mapper;

import id.my.tudemaha.lobos.dto.UserRequest;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.utils.PasswordHasher;

public class UserMapper {
    public static User toEntity(UserRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        String hashedPassword = PasswordHasher.hashPassword(request.getPassword());
        user.setPassword(hashedPassword);

        return user;
    }
}
