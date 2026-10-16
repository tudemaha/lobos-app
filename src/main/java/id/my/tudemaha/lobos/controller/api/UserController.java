package id.my.tudemaha.lobos.controller.api;

import id.my.tudemaha.lobos.dto.request.*;
import id.my.tudemaha.lobos.dto.response.Token;
import id.my.tudemaha.lobos.dto.response.HttpResponse;
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
@Tag(name = "User/Auth", description = "User and auth management endpoints")
public class UserController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public UserController(UserService userService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register endpoint")
    public ResponseEntity<HttpResponse<Void>> registerUser(@Valid @RequestBody UserRegister userRegister) {
        userService.register(userRegister);
        return ResponseEntity.status(HttpStatus.CREATED).body(HttpResponse.success("user registered successfully", null));
    }

    @PostMapping("/login")
    @Operation(summary = "Login endpoint")
    public ResponseEntity<HttpResponse<Token>> login(@Valid @RequestBody UserLogin userLogin) {
        Token token = userService.login(userLogin);
        return ResponseEntity.ok(HttpResponse.success("login successfully", token));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Generate new access token from refresh token")
    public ResponseEntity<HttpResponse<Token>> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
        Token token = userService.refresh(refreshRequest);
        return ResponseEntity.ok(HttpResponse.success("refresh successfully", token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout from current device")
    public ResponseEntity<HttpResponse<Void>> logout(@Valid @RequestBody RefreshRequest refreshRequest) {
        refreshTokenService.revoke(refreshRequest.getRefreshToken());
        return ResponseEntity.ok(HttpResponse.success("logout successfully", null));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all device")
    public ResponseEntity<HttpResponse<Void>> logoutAll(@AuthenticationPrincipal User user) {
        refreshTokenService.revokeAll(user);
        return ResponseEntity.ok(HttpResponse.success("logout from all device successfully", null));
    }

    @PutMapping("/update")
    @Operation(summary = "Update a user's detail")
    public ResponseEntity<HttpResponse<Void>> updateUser(
            @Valid @RequestBody UserUpdate userUpdate,
            @AuthenticationPrincipal User user
            ) {
        userService.update(userUpdate, user.getId());
        return ResponseEntity.ok(HttpResponse.success("profile updated successfully", null));
    }

    @PatchMapping("/email")
    @Operation(summary = "Update a user's email")
    public ResponseEntity<HttpResponse<Void>> updateEmail(
            @Valid @RequestBody UpdateEmail updateEmail,
            @AuthenticationPrincipal User user
    ) {
        userService.updateEmail(updateEmail, user.getId());
        return ResponseEntity.ok(HttpResponse.success("email updated successfully", null));
    }

    @PatchMapping("/password")
    @Operation(summary = "Update a user's password")
    public ResponseEntity<HttpResponse<Void>> updatePassword(
            @Valid @RequestBody UpdatePassword updatePassword,
            @AuthenticationPrincipal User user
            ) {
        userService.updatePassword(updatePassword, user.getId());
        return ResponseEntity.ok(HttpResponse.success("password updated successfully", null));
    }

    @DeleteMapping()
    @Operation(summary = "Delete a user's account")
    public ResponseEntity<HttpResponse<Void>> deleteUser(@AuthenticationPrincipal User user) {
        userService.delete(user.getId());
        return ResponseEntity.ok(HttpResponse.success("user deleted successfully", null));
    }
}
