package id.my.tudemaha.lobos.controller;

import id.my.tudemaha.lobos.dto.request.UserUpdate;
import id.my.tudemaha.lobos.dto.response.AccessToken;
import id.my.tudemaha.lobos.dto.response.HttpResponse;
import id.my.tudemaha.lobos.dto.request.UserLogin;
import id.my.tudemaha.lobos.dto.request.UserRegister;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<HttpResponse<Void>> registerUser(@Valid @RequestBody UserRegister userRegister) {
        userService.register(userRegister);
        return ResponseEntity.status(HttpStatus.CREATED).body(HttpResponse.success("user registered successfully", null));
    }

    @PostMapping("/login")
    public ResponseEntity<HttpResponse<AccessToken>> login(@Valid @RequestBody UserLogin userLogin) {
        AccessToken accessToken = userService.login(userLogin);
        return ResponseEntity.ok(HttpResponse.success("login successfully", accessToken));
    }

    @PutMapping("/update")
    public ResponseEntity<HttpResponse<Void>> updateUser(
            @Valid @RequestBody UserUpdate userUpdate,
            @AuthenticationPrincipal User user
            ) {
        userService.update(userUpdate, user.getId());
        return ResponseEntity.ok(HttpResponse.success("profile updated successfully", null));
    }

    @PatchMapping("/update-email")
    public ResponseEntity<HttpResponse<Void>> updateEmail() {
        return ResponseEntity.ok(HttpResponse.success("email updated successfully", null));
    }

    @PatchMapping("/update-password")
    public ResponseEntity<HttpResponse<Void>> updatePassword() {
        return ResponseEntity.ok(HttpResponse.success("password updated successfully", null));
    }
}
