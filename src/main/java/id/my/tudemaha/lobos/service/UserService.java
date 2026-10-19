package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.request.*;
import id.my.tudemaha.lobos.dto.response.Token;
import id.my.tudemaha.lobos.exception.*;
import id.my.tudemaha.lobos.mapper.UserMapper;
import id.my.tudemaha.lobos.model.EmailVerificationToken;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.repository.EmailVerificationTokenRepository;
import id.my.tudemaha.lobos.repository.UserRepository;
import id.my.tudemaha.lobos.security.JwtService;
import id.my.tudemaha.lobos.utils.Hasher;
import id.my.tudemaha.lobos.utils.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PersistentTokenRepository persistentTokenRepository;
    private final EmailService emailService;

    @Value("${app.verification.ttl}")
    private Duration verificationTtl;

    public UserService(
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PersistentTokenRepository persistentTokenRepository,
            EmailService emailService,
            EmailVerificationTokenRepository emailVerificationTokenRepository
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.persistentTokenRepository = persistentTokenRepository;
        this.emailService = emailService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    @Transactional
    public void register(UserRegister userRegister) {
        if(!userRegister.getPassword().equals(userRegister.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and password confirmation do not match");
        }

        User newUser = UserMapper.toEntity(userRegister);
        newUser.setId(UUID.randomUUID().toString());

        Optional<User> user = userRepository.findByEmail(newUser.getEmail());
        if(user.isPresent()) {
            throw new DuplicateEmailException(newUser.getEmail());
        }

        userRepository.save(newUser);
        String rawToken = generateAndSaveToken(newUser.getId(), "REGISTER");
        emailService.sendEmail(newUser.getEmail(), "REGISTER", newUser.getFirstName(), rawToken);
    }

    public User authenticate(UserLogin userLogin) {
        User user = userRepository.findByEmail(userLogin.getEmail())
                .orElseThrow(LoginException::new);

        if (!Hasher.checkPassword(userLogin.getPassword(), user.getPassword())) {
            throw new LoginException();
        }

        if (user.getEmailVerifiedAt() == null) {
            throw new EmailNotVerifiedException();
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

    @Transactional
    public void verifyEmail(String rawToken) {
        String hashToken = Hasher.hashToken(rawToken);
        EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository.findByTokenHash(hashToken)
                .orElseThrow(() -> new InvalidTokenException("invalid or expired verification token"));

        if (
                emailVerificationToken.getExpiresAt().isBefore(LocalDateTime.now()) ||
                emailVerificationToken.getUsedAt() != null
        ) {
            throw new InvalidTokenException("invalid or expired verification token");
        }

        User user = userRepository.findById(emailVerificationToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("invalid or expired verification token"));

        switch (emailVerificationToken.getPurpose()) {
            case "REGISTER":
                user.setEmailVerifiedAt(LocalDateTime.now());
                break;
            case "CHANGE_EMAIL":
                if (user.getPendingEmail() == null) {
                    throw new InvalidTokenException("No pending email change found");
                }

                userRepository
                        .findByEmail(user.getPendingEmail())
                        .ifPresent(checkUser -> {
                            throw new DuplicateEmailException(user.getPendingEmail());
                        });

                user.setEmail(user.getPendingEmail());
                user.setPendingEmail(null);
                persistentTokenRepository.removeUserTokens(user.getId());
                refreshTokenService.revokeAll(emailVerificationToken.getUserId());
                break;
            default:
                throw new InvalidTokenException("invalid or expired verification token");
        }

        userRepository.update(user);
        emailVerificationTokenRepository.markAsUsed(emailVerificationToken.getId());
    }

    public void resendEmailVerification(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String rawToken = generateAndSaveToken(user.getId(), "REGISTER");
            emailService.sendEmail(user.getEmail(), "REGISTER", user.getFirstName(), rawToken);
        }
    }

    public void forgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String rawToken = generateAndSaveToken(user.getId(), "FORGET");
            emailService.sendEmail(user.getEmail(), "FORGET", user.getFirstName(), rawToken);
        }
    }

    @Transactional
    public void resetPassword(ResetPassword resetPassword) {
        if (!resetPassword.getPassword().equals(resetPassword.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and password confirmation do not match");
        }

        String hashToken = Hasher.hashToken(resetPassword.getToken());
        EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository.findByTokenHash(hashToken)
                .orElseThrow(() -> new InvalidTokenException("invalid or expired reset password token"));

        if (
                emailVerificationToken.getUsedAt() != null ||
                emailVerificationToken.getExpiresAt().isBefore(LocalDateTime.now())
        ) {
            throw new InvalidTokenException("invalid or expired reset password token");
        }

        if (!"FORGET".equals(emailVerificationToken.getPurpose())) {
            throw new InvalidTokenException("invalid or expired reset password token");
        }


        User user = userRepository.findById(emailVerificationToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("invalid or expired reset password token"));
        user.setPassword(Hasher.hashPassword(resetPassword.getPassword()));
        userRepository.update(user);
        emailVerificationTokenRepository.markAsUsed(emailVerificationToken.getId());

        persistentTokenRepository.removeUserTokens(user.getId());
        refreshTokenService.revokeAll(user.getId());
    }

    public Token refresh(RefreshRequest refreshRequest) {
        RefreshTokenService.TokenPair tokenPair = refreshTokenService.rotateRefreshToken(refreshRequest.getRefreshToken());
        String accessToken = jwtService.generateToken(tokenPair.user());

        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(tokenPair.refreshToken());
        return token;
    }

    public User findById(String id) {
        return userRepository.findById(id).orElseThrow(NotFoundException::new);
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

    @Transactional
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
        user.setPendingEmail(updateEmail.getEmail());
        userRepository.update(user);
        String rawToken = generateAndSaveToken(user.getId(), "CHANGE_EMAIL");
        emailService.sendEmail(user.getPendingEmail(), "CHANGE_EMAIL", user.getFirstName(), rawToken);
    }

    @Transactional
    public void updatePassword(UpdatePassword updatePassword, String id) {
        User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
        boolean match = Hasher.checkPassword(updatePassword.getOldPassword(), user.getPassword());
        if(!match) {
            throw new ForbiddenAccessException("invalid old password");
        }

        if(!updatePassword.getNewPassword().equals(updatePassword.getConfirmNewPassword())) {
            throw new IllegalArgumentException("newPassword and confirmNewPassword do not match");
        }

        String hashedPassword = Hasher.hashPassword(updatePassword.getNewPassword());
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

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired verification tokens");
        int deleteCount = emailVerificationTokenRepository.deleteExpiredTokens();
        log.info("Finished cleanup: deleted {} expired verification tokens", deleteCount);
    }

    private String generateAndSaveToken(String userId, String purpose) {
        String rawToken = TokenUtil.generate(32);
        String tokenHash = Hasher.hashToken(rawToken);

        LocalDateTime expiresAt = LocalDateTime.now().plus(verificationTtl);
        EmailVerificationToken token = new EmailVerificationToken(
                null,
                userId,
                tokenHash,
                purpose,
                null,
                expiresAt,
                null
        );

        emailVerificationTokenRepository.insert(token);
        return rawToken;
    }
}
