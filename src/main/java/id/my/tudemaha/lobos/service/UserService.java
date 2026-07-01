package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.request.*;
import id.my.tudemaha.lobos.dto.response.AccessToken;
import id.my.tudemaha.lobos.exception.DuplicateEmailException;
import id.my.tudemaha.lobos.exception.ForbiddenAccessException;
import id.my.tudemaha.lobos.exception.LoginException;
import id.my.tudemaha.lobos.exception.UserNotFoundException;
import id.my.tudemaha.lobos.mapper.UserMapper;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.repository.UserRepository;
import id.my.tudemaha.lobos.security.JwtService;
import id.my.tudemaha.lobos.utils.PasswordHasher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public void register(UserRegister userRegister) {
        if(!userRegister.getPassword().equals(userRegister.getConfirmPassword())) {
            throw new IllegalArgumentException("password and confirmPassword do not match");
        }

        User newUser = UserMapper.toEntity(userRegister);

        Optional<User> user = userRepository.findByEmail(newUser.getEmail());
        if(user.isPresent()) {
            throw new DuplicateEmailException(newUser.getEmail());
        }

        userRepository.save(newUser);
    }

    public AccessToken login(UserLogin userLogin) {
        Optional<User> userOpt = userRepository.findByEmail(userLogin.getEmail());
        if (userOpt.isEmpty()) {
            throw new LoginException();
        }

        User user = userOpt.get();
        if (!PasswordHasher.checkPassword(userLogin.getPassword(), user.getPassword())) {
            throw new LoginException();
        }
        String token = jwtService.generateToken(user);

        AccessToken accessToken = new AccessToken();
        accessToken.setToken(token);
        return accessToken;
    }

    public void update(UserUpdate userUpdate, String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException();
        }

        User user = userOpt.get();
        user.setFirstName(userUpdate.getFirstName());
        user.setLastName(userUpdate.getLastName());
        userRepository.update(user);
    }

    public void updateEmail(UpdateEmail updateEmail, String id) {
        Optional<User> userOpt = userRepository.findByEmail(updateEmail.getEmail());
        if (userOpt.isPresent()) {
            throw new DuplicateEmailException(updateEmail.getEmail());
        }

        userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException();
        }

        User user = userOpt.get();
        user.setEmail(updateEmail.getEmail());
        userRepository.update(user);
    }

    public void updatePassword(UpdatePassword updatePassword, String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException();
        }

        User user = userOpt.get();
        boolean match = PasswordHasher.checkPassword(updatePassword.getOldPassword(), user.getPassword());
        if(!match) {
            throw new ForbiddenAccessException("invalid old password");
        }

        if(!updatePassword.getNewPassword().equals(updatePassword.getConfirmNewPassword())) {
            throw new IllegalArgumentException("newPassword and confirmNewPassword do not match");
        }

        String hashedPassword = PasswordHasher.hashPassword(updatePassword.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.update(user);
    }

    public void delete(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException();
        }

        userRepository.delete(id);
    }
}
