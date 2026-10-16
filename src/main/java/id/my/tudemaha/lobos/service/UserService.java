package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.request.*;
import id.my.tudemaha.lobos.dto.response.Token;
import id.my.tudemaha.lobos.exception.DuplicateEmailException;
import id.my.tudemaha.lobos.exception.ForbiddenAccessException;
import id.my.tudemaha.lobos.exception.LoginException;
import id.my.tudemaha.lobos.exception.NotFoundException;
import id.my.tudemaha.lobos.mapper.UserMapper;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.repository.UserRepository;
import id.my.tudemaha.lobos.security.JwtService;
import id.my.tudemaha.lobos.utils.PasswordHasher;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PersistentTokenRepository persistentTokenRepository;

    public UserService(
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PersistentTokenRepository persistentTokenRepository
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.persistentTokenRepository = persistentTokenRepository;
    }

    public void register(UserRegister userRegister) {
        if(!userRegister.getPassword().equals(userRegister.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and password confirmation do not match");
        }

        User newUser = UserMapper.toEntity(userRegister);

        Optional<User> user = userRepository.findByEmail(newUser.getEmail());
        if(user.isPresent()) {
            throw new DuplicateEmailException(newUser.getEmail());
        }

        userRepository.save(newUser);
    }

    public User authenticate(UserLogin userLogin) {
        User user = userRepository.findByEmail(userLogin.getEmail())
                .orElseThrow(LoginException::new);

        if (!PasswordHasher.checkPassword(userLogin.getPassword(), user.getPassword())) {
            throw new LoginException();
        }

        return user;
    }

    public Token login(UserLogin userLogin) {
        User user = authenticate(userLogin);
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.generate(user.getId(), "");

        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        return token;
    }

    public Token refresh(RefreshRequest refreshRequest) {
        RefreshTokenService.TokenPair tokenPair = refreshTokenService.rotateRefreshToken(refreshRequest.getRefreshToken());
        String accessToken = jwtService.generateToken(tokenPair.user());

        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(tokenPair.refreshToken());
        return token;
    }

    public void update(UserUpdate userUpdate, String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new NotFoundException();
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
            throw new NotFoundException();
        }

        User user = userOpt.get();
        user.setEmail(updateEmail.getEmail());
        userRepository.update(user);
    }

    @Transactional
    public void updatePassword(UpdatePassword updatePassword, String id) {
        User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
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
        if (updatePassword.isWithLogoutAll()) {
            persistentTokenRepository.removeUserTokens(user.getId());
            refreshTokenService.revokeAll(user.getId());
        }
    }

    @Transactional
    public void delete(String id) {
        User user = userRepository.findById(id).orElseThrow(NotFoundException::new);

        persistentTokenRepository.removeUserTokens(user.getId());
        refreshTokenService.revokeAll(user.getId());
        userRepository.delete(user.getId());
    }
}
