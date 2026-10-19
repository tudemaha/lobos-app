package id.my.tudemaha.lobos.mapper;

import id.my.tudemaha.lobos.dto.request.UserRegister;
import id.my.tudemaha.lobos.dto.response.UserProfile;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.utils.Hasher;

public class UserMapper {
    public static User toEntity(UserRegister request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        String hashedPassword = Hasher.hashPassword(request.getPassword());
        user.setPassword(hashedPassword);

        return user;
    }

    public static UserProfile toProfile(User user) {
        return new UserProfile(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPendingEmail(),
                user.getEmailVerifiedAt(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
