package id.my.tudemaha.lobos.controller.api;

import id.my.tudemaha.lobos.dto.request.*;
import id.my.tudemaha.lobos.dto.response.HttpResponse;
import id.my.tudemaha.lobos.dto.response.Token;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.RefreshTokenService;
import id.my.tudemaha.lobos.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication and session management endpoints")
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register endpoint")
    public ResponseEntity<HttpResponse<Void>> registerUser(@Valid @RequestBody UserRegister userRegister) {
        userService.register(userRegister);
        return ResponseEntity.status(HttpStatus.CREATED).body(HttpResponse.success());
    }

    @PostMapping("/login")
    @Operation(summary = "Login endpoint")
    public ResponseEntity<HttpResponse<Token>> login(@Valid @RequestBody UserLogin userLogin) {
        Token token = userService.login(userLogin);
        return ResponseEntity.ok(HttpResponse.success(token));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify newly created account or email changes")
    public ResponseEntity<HttpResponse<Void>> verifyEmail(@RequestParam String t) {
        userService.verifyEmail(t);
        return ResponseEntity.ok(HttpResponse.success());
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification")
    public ResponseEntity<HttpResponse<Void>> resendVerification(@Valid @RequestBody UpdateEmail email) {
        userService.resendEmailVerification(email.getEmail());
        return ResponseEntity.ok(HttpResponse.success());
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<HttpResponse<Void>> forgotPassword(@Valid @RequestBody UpdateEmail email) {
        userService.forgotPassword(email.getEmail());
        return ResponseEntity.ok(HttpResponse.success());
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset user password")
    public ResponseEntity<HttpResponse<Void>> resetPassword(@Valid @RequestBody ResetPassword resetPassword) {
        userService.resetPassword(resetPassword);
        return ResponseEntity.ok(HttpResponse.success());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Generate new access token from refresh token")
    public ResponseEntity<HttpResponse<Token>> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
        Token token = userService.refresh(refreshRequest);
        return ResponseEntity.ok(HttpResponse.success(token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout from current device")
    public ResponseEntity<HttpResponse<Void>> logout(@Valid @RequestBody RefreshRequest refreshRequest) {
        refreshTokenService.revoke(refreshRequest.getRefreshToken());
        return ResponseEntity.ok(HttpResponse.success());
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices")
    public ResponseEntity<HttpResponse<Void>> logoutAll(@AuthenticationPrincipal User user) {
        refreshTokenService.revokeAll(user);
        return ResponseEntity.ok(HttpResponse.success());
    }
}
